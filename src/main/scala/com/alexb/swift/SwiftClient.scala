package com.alexb.swift

import akka.actor._
import akka.pattern.pipe
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Promise, Future}
import spray.client.HttpClient
import spray.httpx.UnsuccessfulResponseException
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
    props = Props(new HttpClient),
    name = "http-client")
  private var authenticationResult: Future[AuthenticationResult] = null
  private var authenticationRevision = 0

  def receive = {
    case ListContainers =>
      executeRequest((auth, conduit) => listContainers(auth.storageUrl.toString, auth.token, conduit))

    case ListObjects(container) =>
      executeRequest((auth, conduit) => listObjects(auth.storageUrl.toString, container, auth.token, conduit))

    case CreateContainer(container) =>
      executeRequest((auth, conduit) => createContainer(auth.storageUrl.toString, container, auth.token, conduit))

    case DeleteContainer(container) =>
      executeRequest((auth, conduit) => deleteContainer(auth.storageUrl.toString, container, auth.token, conduit))

    case GetObject(container, name) =>
      executeRequest((auth, conduit) => getObject(auth.storageUrl.toString, container, name, auth.token, conduit))

    case PutObject(container, name, mediaType, data) =>
      executeRequest((auth, conduit) => putObject(auth.storageUrl.toString, container, name, mediaType, data, auth.token, conduit))

    case DeleteObject(container, name) =>
      executeRequest((auth, conduit) => deleteObject(auth.storageUrl.toString, container, name, auth.token, conduit))

    case NotifyExpiredAuthentication(rev) => refreshAuthentication(rev)

    case msg: RetryRequest[_] => retryRequest(msg)
  }

  override def preStart() {
    refreshAuthentication(0)
  }

  private def refreshAuthentication(lastSeenRevision: Int) {
    if (authenticationRevision == lastSeenRevision) {
      log.debug(s"About to refresh authentication. Current revision: $authenticationRevision")
      authenticationResult = authenticate(httpClient, credentials, authHost, authPort, authSslEnabled)
      authenticationRevision += 1
    }
  }

  private def executeRequest[R](action: (AuthenticationResult, ActorRef) => Future[R]) {
    val currentRevision = authenticationRevision
    val resultFuture = doExecuteRequest(action)
    val promise = Promise[R]()
    resultFuture onFailure {
      case e: UnsuccessfulResponseException if e.responseStatus == StatusCodes.Unauthorized => {
        self ! NotifyExpiredAuthentication(currentRevision)
        self ! RetryRequest(action, promise)
      }
    }
    resultFuture onSuccess { case r =>
      promise.success(r)
    }
    promise.future.pipeTo(sender)
  }

  private def retryRequest[R](req: RetryRequest[R]) {
    log.debug("Retrying request due to expired authentication}")
    req.promise.completeWith(doExecuteRequest(req.action))
  }

  private def doExecuteRequest[R](action: (AuthenticationResult, ActorRef) => Future[R]): Future[R] =
    authenticationResult.flatMap(auth => action(auth, httpClient))
}

private[swift] case class AuthenticationResult(token: String, storageUrl: Url)
private[swift] case class RetryRequest[R](action: (AuthenticationResult, ActorRef) => Future[R], promise: Promise[R])
