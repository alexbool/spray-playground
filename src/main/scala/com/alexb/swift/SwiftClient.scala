package com.alexb.swift

import akka.actor.{ActorRef, ActorLogging, Props, Actor}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import spray.can.client.HttpClient
import spray.client.UnsuccessfulResponseException
import spray.io.IOExtension
import spray.http.StatusCodes

class SwiftClient(credentials: SwiftCredentials,
                  authHost: String,
                  authPort: Int = 80,
                  authSslEnabled: Boolean = false)
  extends Actor with ActorLogging with AuthenticationActions
  with AccountActions with ContainerActions with ObjectActions {

  private case class NotifyExpiredAuthentication(lastSeenRevision: Int)

  implicit val timeout = Timeout(10 seconds)
  implicit val ctx = context.dispatcher

  private val httpClient = context.actorOf(
    props = Props(new HttpClient(IOExtension(context.system).ioBridge())),
    name = "http-client")
  private val conduitFactory = context.actorOf(
    props = Props(new ConduitFactory(httpClient)),
    name = "http-conduit-factory")
  private var authenticationResult: Option[Future[AuthenticationResult]] = None
  private var authenticationRevision = 0

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

    case NotifyExpiredAuthentication(rev) =>
      if (authenticationRevision == rev) authenticationResult = None
  }

  private def authentication() = authenticationResult match {
    case Some(auth) => auth
    case None => {
      authenticationResult = Some(authenticate(httpClient, credentials, authHost, authPort, authSslEnabled))
      authenticationRevision += 1
      authenticationResult.get
    }
  }

  private def executeRequest[R](action: (AuthenticationResult, ActorRef) => Future[R]) {
    val currentRevision = authenticationRevision
    val authFuture = authentication()
    val resultFuture = for {
      auth <- authFuture
      conduit <- (conduitFactory ? HttpConduitId(auth.storageUrl.host, auth.storageUrl.port, auth.storageUrl.sslEnabled)).mapTo[ActorRef]
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
