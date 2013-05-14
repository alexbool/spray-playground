package com.alexb.swift

import akka.actor._
import language.postfixOps

class SwiftClient(credentials: Credentials, authUrl: String)
  extends Actor with ActorLogging {

  val authenticator = context.actorOf(Props(new Authenticator(credentials, authUrl)), "authenticator")
  var authInProgress = false
  var cachedAuth: Option[AuthenticationResult] = None
  val workerCounter = Iterator from 0

  def receive = {
    case ListContainers =>
      executeRequest(new ListContainersAction)

    case ListObjects(container) =>
      executeRequest(new ListObjectsAction(container))

    case CreateContainer(container) =>
      executeRequest(new CreateContainerAction(container))

    case DeleteContainer(container) =>
      executeRequest(new DeleteContainerAction(container))

    case GetObject(container, name) =>
      executeRequest(new GetObjectAction(container, name))

    case PutObject(container, name, mediaType, data) =>
      executeRequest(new PutObjectAction(container, name, mediaType, data))

    case DeleteObject(container, name) =>
      executeRequest(new DeleteObjectAction(container, name))

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
    log.debug(s"Creating new worker: $name for action: $action")
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
