package com.alexb.swift

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.actor.Status.Failure
import akka.io.IO
import spray.can.Http
import spray.httpx.UnsuccessfulResponseException
import spray.http.{HttpResponse, StatusCodes}

private[swift] class Worker[R](action: Action[R], recipient: ActorRef) extends Actor with ActorLogging {
  val httpTransport = IO(Http)(context.system)
  var inProgress = false
  var currentAuth: Option[AuthenticationResult] = None
  var fresherAuth: Option[AuthenticationResult] = None

  def receive = {
    case GotAuthentication(auth) => handleGotAuthentication(auth)
    case BadCredentials          => recipient ! Failure(new BadCredentialsException)
    case AuthenticationFailed(e) => recipient ! Failure(new SwiftException(e))
    case r: HttpResponse         => handleResponse(r)
    case Failure(e)              => handleFailure(e)
  }

  def handleGotAuthentication(auth: AuthenticationResult) {
    log.debug(s"Got authentication: $auth")
    if (!inProgress)
      executeAction(auth)
    else
      fresherAuth = Some(auth)
  }

  def retry() {
    currentAuth = None
    fresherAuth match {
      case Some(auth) => executeAction(auth)
      case None       => inProgress = false
    }
  }

  def executeAction(auth: AuthenticationResult) {
    log.debug("Executing action")
    currentAuth = Some(auth)
    inProgress = true
    httpTransport ! action.buildRequest(auth)
  }

  def handleResponse(response: HttpResponse) {
    if (response.status == StatusCodes.Unauthorized) {
      // Notify that current authentication expired, wait for another GotAuthentication message
      context.parent ! AuthenticationExpired(currentAuth.get.revision)
      retry()
    } else if (response.status.isSuccess || response.status == StatusCodes.NotFound) {
      val result = action.parseResponse(response)
      log.debug(s"Successful result: $result, stopping")
      recipient ! result
      context.stop(self)
    } else {
      handleFailure(new UnsuccessfulResponseException(response))
    }
  }

  def handleFailure(error: Throwable) {
    log.warning(s"Error when performing request: $error, stopping")
    recipient ! Failure(new SwiftException(error))
    context.stop(self)
  }
}
