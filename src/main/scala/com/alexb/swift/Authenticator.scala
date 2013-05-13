package com.alexb.swift

import akka.actor.{ActorLogging, Actor}
import akka.actor.Status.Failure
import akka.util.Timeout
import akka.io.IO
import spray.can.Http
import spray.httpx.UnsuccessfulResponseException
import spray.http.{HttpResponse, StatusCodes}

private[swift] class Authenticator(credentials: Credentials, authUrl: String)(implicit futureTimeout: Timeout)
  extends Actor with Authentication with ActorLogging {

  val httpTransport = IO(Http)(context.system)

  val revisionCounter = Iterator from 0
  var currentRevision = 0

  def receive = {
    case TryAuthenticate            => tryAuthenticate()
    case AuthenticationExpired(rev) => if (rev == currentRevision) tryAuthenticate()
    case r: HttpResponse            => handleAuthResponse(r)
    case Failure(e)                 => handleAuthFailure(e)
  }

  def tryAuthenticate() {
    log.debug(s"About to refresh authentication. Current revision: $currentRevision")
    currentRevision = revisionCounter.next()
    httpTransport ! authenticationRequest(credentials, authUrl)
  }

  def handleAuthResponse(response: HttpResponse) {
    if (response.status.isSuccess) {
      val auth = authResultFromResponse(response, currentRevision)
      log.debug(s"Received successful authentication: $auth")
      context.parent ! GotAuthentication(auth)
    } else if (response.status == StatusCodes.Unauthorized) {
      log.error("Bad credentials")
      context.parent ! BadCredentials
    } else {
      handleAuthFailure(new UnsuccessfulResponseException(response))
    }
  }

  def handleAuthFailure(error: Throwable) {
    log.warning(s"Authentication failed: $error")
    context.parent ! AuthenticationFailed(error)
  }
}
