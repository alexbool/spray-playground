package com.alexb.swift

import akka.actor.{ActorLogging, Actor}
import akka.actor.Status.Failure
import akka.io.IO
import spray.can.Http
import spray.httpx.UnsuccessfulResponseException
import spray.http.{HttpRequest, HttpResponse, StatusCodes}
import spray.client.pipelining._

private[swift] class Authenticator(credentials: Credentials, authUrl: String)
  extends Actor with ActorLogging {

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

  def authenticationRequest(credentials: Credentials, authUrl: String): HttpRequest =
    Get(authUrl) ~> addHeader("X-Auth-User", credentials.user) ~> addHeader("X-Auth-Key", credentials.key)

  def authResultFromResponse(response: HttpResponse, revision: Int) =
    AuthenticationResult(
      response.headers.find(_.is("x-auth-token")).map(_.value).get,
      response.headers.find(_.is("x-storage-url")).map(_.value).get,
      revision)
}
