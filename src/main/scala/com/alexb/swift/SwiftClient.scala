package com.alexb.swift

import akka.actor._
import akka.pattern.pipe
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Promise, Future}
import spray.client.HttpClient
import spray.httpx.UnsuccessfulResponseException
import spray.http.StatusCodes

class SwiftClient(credentials: SwiftCredentials, authUrl: String)
  extends Actor with ActorLogging with SwiftAuthentication
  with AccountActions with ContainerActions with ObjectActions {

  type Action[R] = AuthenticationResult => Future[R]

  private case class NotifyExpiredAuthentication(lastSeenRevision: Int)
  private case class RetryRequest[R](action: Action[R], promise: Promise[R])

  implicit val timeout = Timeout(10 seconds)
  implicit val ctx = context.dispatcher

  private val httpClient = context.actorOf(
    props = Props(new HttpClient),
    name = "http-client")
  private var authenticationResult: Future[AuthenticationResult] = null
  private var authenticationRevision = 0

  def receive = {
    case ListContainers =>
      executeRequest(auth => listContainers(auth.storageUrl, auth.token, httpClient))

    case ListObjects(container) =>
      executeRequest(auth => listObjects(auth.storageUrl, container, auth.token, httpClient))

    case CreateContainer(container) =>
      executeRequest(auth => createContainer(auth.storageUrl, container, auth.token, httpClient))

    case DeleteContainer(container) =>
      executeRequest(auth => deleteContainer(auth.storageUrl, container, auth.token, httpClient))

    case GetObject(container, name) =>
      executeRequest(auth => getObject(auth.storageUrl, container, name, auth.token, httpClient))

    case PutObject(container, name, mediaType, data) =>
      executeRequest(auth => putObject(auth.storageUrl, container, name, mediaType, data, auth.token, httpClient))

    case DeleteObject(container, name) =>
      executeRequest(auth => deleteObject(auth.storageUrl, container, name, auth.token, httpClient))

    case NotifyExpiredAuthentication(rev) => refreshAuthentication(rev)

    case msg: RetryRequest[_] => retryRequest(msg)
  }

  override def preStart() {
    refreshAuthentication(0)
  }

  private def refreshAuthentication(lastSeenRevision: Int) {
    if (authenticationRevision == lastSeenRevision) {
      log.debug(s"About to refresh authentication. Current revision: $authenticationRevision")
      authenticationResult = authenticate(httpClient, credentials, authUrl)
      authenticationRevision += 1
    }
  }

  private def executeRequest[R](action: Action[R]) {
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

  private def doExecuteRequest[R](action: Action[R]): Future[R] =
    authenticationResult.flatMap(action(_))
}

private[swift] case class AuthenticationResult(token: String, storageUrl: String)
