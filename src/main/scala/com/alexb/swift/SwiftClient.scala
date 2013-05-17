package com.alexb.swift

import akka.actor._

class SwiftClient(credentials: Credentials, authUrl: String) extends Actor with ActorLogging {
  private val authenticator = context.actorOf(Props(new Authenticator(credentials, authUrl)), "authenticator")
  private var authInProgress = false
  private var cachedAuth: Option[AuthenticationResult] = None
  private val workerCounter = Iterator from 0

  def receive = {
    case msg: Request               => executeRequest(actionForRequest(msg))
    case msg: GotAuthentication     => handleGotAuthentication(msg)
    case msg: AuthenticationFailed  => handleAuthenticationFailed(msg)
    case msg: AuthenticationExpired => handleAuthenticationExpired(msg)
    case BadCredentials             => handleBadCredentials()
  }

  def executeRequest[R](action: Action[R]) {
    if (cachedAuth.isEmpty && !authInProgress) {
      authInProgress = true
      authenticator ! TryAuthenticate
    }
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

  def actionForRequest(request: Request) = request match {
    case ListContainers     => new ListContainersAction
    case r: ListObjects     => new ListObjectsAction(r)
    case r: CreateContainer => new CreateContainerAction(r)
    case r: DeleteContainer => new DeleteContainerAction(r)
    case r: GetObject       => new GetObjectAction(r)
    case r: PutObject       => new PutObjectAction(r)
    case r: DeleteObject    => new DeleteObjectAction(r)
  }

  def newWorker[R](action: Action[R], recipient: ActorRef) = {
    val name = s"worker-${workerCounter.next()}"
    log.debug(s"Creating new worker: $name for action: $action")
    context.actorOf(Props(new Worker(action, recipient)), name)
  }

  def workers = context.actorSelection("worker-*")
}
