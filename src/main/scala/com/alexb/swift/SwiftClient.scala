package com.alexb.swift

import akka.actor._
import akka.pattern.pipe
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import spray.can.client.HttpClient
import spray.client.{HttpConduit, UnsuccessfulResponseException}
import spray.io.IOExtension
import spray.http.StatusCodes

class SwiftClient(credentials: SwiftCredentials,
                  authHost: String,
                  authPort: Int = 80,
                  authSslEnabled: Boolean = false)
  extends Actor with ActorLogging with SwiftAuthentication
  with AccountActions with ContainerActions with ObjectActions {

  private case class NotifyExpiredAuthentication(lastSeenRevision: Int)

  implicit val timeout = Timeout(10 seconds)
  implicit val ctx = context.dispatcher

  private val httpClient = context.actorOf(
    props = Props(new HttpClient(IOExtension(context.system).ioBridge())),
    name = "http-client")
  private var authenticationResult: Future[AuthenticationResult] = null
  private var authenticationRevision = 0
  private var storageConduit: Future[ActorRef] = null

  def receive = {
    case ListContainers =>
      executeRequest((auth, conduit) => listContainers(auth.storageUrl.path, auth.token, conduit))

    case ListObjects(container) =>
      executeRequest((auth, conduit) => listObjects(auth.storageUrl.path, container, auth.token, conduit))

    case CreateContainer(container) =>
      executeRequest((auth, conduit) => createContainer(auth.storageUrl.path, container, auth.token, conduit))

    case DeleteContainer(container) =>
      executeRequest((auth, conduit) => deleteContainer(auth.storageUrl.path, container, auth.token, conduit))

    case GetObject(container, name) =>
      executeRequest((auth, conduit) => getObject(auth.storageUrl.path, container, name, auth.token, conduit))

    case PutObject(container, name, mediaType, data) =>
      executeRequest((auth, conduit) => putObject(auth.storageUrl.path, container, name, mediaType, data, auth.token, conduit))

    case DeleteObject(container, name) =>
      executeRequest((auth, conduit) => deleteObject(auth.storageUrl.path, container, name, auth.token, conduit))

    case NotifyExpiredAuthentication(rev) => refreshAuthentication(rev)
  }

  override def preStart() {
    refreshAuthentication(0)
  }

  private def refreshAuthentication(lastSeenRevision: Int) {
    if (authenticationRevision == lastSeenRevision) {
      log.debug(s"About to refresh authentication. Current revision: ${authenticationRevision}")
      authenticationResult = authenticate(httpClient, credentials, authHost, authPort, authSslEnabled)
      authenticationRevision += 1
      if (storageConduit != null) storageConduit onSuccess { case conduit: ActorRef =>
        // Shutting down existing conduit after a reasonable timeout
        log.debug(s"Scheduling existing HttpConduit ${conduit.toString()} shutdown")
        context.system.scheduler.scheduleOnce(1 minute)({
          log.debug(s"Shutting down HttpConduit ${conduit.toString()}")
          context.stop(conduit)
        })
      }
      val currentRev = authenticationRevision
      storageConduit = authenticationResult map { auth =>
        val u = auth.storageUrl
        log.debug("Creating new HttpConduit")
        context.actorOf(
          props = Props(new HttpConduit(httpClient, u.host, u.port, u.sslEnabled)),
          name = s"http-conduit-${u.host}-${u.port}-${ if (u.sslEnabled) "ssl" else "nossl" }-$currentRev")
      }
    }
  }

  private def executeRequest[R](action: (AuthenticationResult, ActorRef) => Future[R]) {
    val currentRevision = authenticationRevision
    val authFuture = authenticationResult
    val storageConduitFuture = storageConduit
    val resultFuture = for {
      auth <- authFuture
      conduit <- storageConduitFuture
      result <- action(auth, conduit)
    } yield result
    resultFuture onFailure {
      case e: UnsuccessfulResponseException if e.responseStatus == StatusCodes.Unauthorized =>
        self ! NotifyExpiredAuthentication(currentRevision)
    }
    resultFuture.pipeTo(sender)
  }
}

private[swift] case class AuthenticationResult(token: String, storageUrl: Url)
