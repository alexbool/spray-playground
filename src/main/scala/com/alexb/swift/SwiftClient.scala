package com.alexb.swift

import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._
import language.postfixOps

class SwiftClient(credentials: Credentials, authUrl: String)(implicit futureTimeout: Timeout = 10 seconds)
  extends Actor with ActorLogging with AccountActions with ContainerActions with ObjectActions {

  import context.dispatcher

  val authenticator = context.actorOf(Props(new Authenticator(credentials, authUrl)), "authenticator")
  var authInProgress = false
  var cachedAuth: Option[AuthenticationResult] = None
  val workerCounter = Iterator from 0

  def receive = {
    case ListContainers =>
      executeRequest(auth => listContainers(auth.storageUrl, auth.token))

    case ListObjects(container) =>
      executeRequest(auth => listObjects(auth.storageUrl, container, auth.token))

    case CreateContainer(container) =>
      executeRequest(auth => createContainer(auth.storageUrl, container, auth.token))

    case DeleteContainer(container) =>
      executeRequest(auth => deleteContainer(auth.storageUrl, container, auth.token))

    case GetObject(container, name) =>
      executeRequest(auth => getObject(auth.storageUrl, container, name, auth.token))

    case PutObject(container, name, mediaType, data) =>
      executeRequest(auth => putObject(auth.storageUrl, container, name, mediaType, data, auth.token))

    case DeleteObject(container, name) =>
      executeRequest(auth => deleteObject(auth.storageUrl, container, name, auth.token))

    case msg: GotAuthentication     => handleGotAuthentication(msg)
    case msg: AuthenticationFailed  => handleAuthenticationFailed(msg)
    case msg: AuthenticationExpired => handleAuthenticationExpired(msg)
    case BadCredentials             => handleBadCredentials()
  }

  def executeRequest[R](action: Action[R]) {
    tryAuthenticateIfNeeded()
    val worker = newWorker(action, sender)
    if (cachedAuth.isDefined) worker ! GotAuthentication(cachedAuth.get)
  }

  def handleGotAuthentication(msg: GotAuthentication) {
    authInProgress = false
    log.debug(s"Got fresh authentication: ${msg.auth}")
    cachedAuth = Some(msg.auth)
    workers ! msg
  }

  def handleBadCredentials() {
    log.error("Bad credentials, stopping")
    workers ! BadCredentials
    context.stop(self)
  }

  def handleAuthenticationFailed(msg: AuthenticationFailed) {
    log.warning("Authentication failed")
    authInProgress = false
    workers ! msg
  }

  def handleAuthenticationExpired(msg: AuthenticationExpired) {
    if (cachedAuth.isDefined && cachedAuth.get.revision == msg.revision) {
      log.debug(s"Handling expiration of authentication revision ${msg.revision}")
      cachedAuth = None
    }
    authenticator ! msg
  }

  def newWorker[R](action: Action[R], recipient: ActorRef) = {
    val name = s"worker-${workerCounter.next()}"
    log.debug(s"Creating new worker for request: $name")
    context.actorOf(Props(new Worker(action, recipient)), name)
  }

  def workers = context.actorSelection("worker-*")

  def tryAuthenticateIfNeeded() {
    if (cachedAuth.isEmpty && !authInProgress) {
      authInProgress = true
      authenticator ! TryAuthenticate
    }
  }
}
