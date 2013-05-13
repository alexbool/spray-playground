package com.alexb.swift

import akka.actor.{ActorLogging, Actor}
import akka.util.Timeout
import spray.httpx.UnsuccessfulResponseException
import spray.http.StatusCodes
import scala.util.control.NonFatal

private[swift] class Authenticator(credentials: Credentials, authUrl: String)(implicit futureTimeout: Timeout)
  extends Actor with Authentication with ActorLogging {

  import context.dispatcher

  var revision = 0

  def receive = {
    case TryAuthenticate            => tryAuthenticate()
    case AuthenticationExpired(rev) => if (rev == revision) tryAuthenticate()
  }

  def tryAuthenticate() {
    log.debug(s"About to refresh authentication. Current revision: $revision")
    revision += 1
    val resultF = authenticate(credentials, authUrl, revision)
    resultF onSuccess { case r =>
      log.debug(s"Successful authentication: $r")
      context.parent ! GotAuthentication(r)
    }
    resultF onFailure {
      case e: UnsuccessfulResponseException if e.response.status == StatusCodes.Unauthorized => {
        log.error("Bad credentials")
        context.parent ! BadCredentials
      }
      case NonFatal(e) => {
        log.warning(s"Authentication failed: $e")
        context.parent ! AuthenticationFailed(e)
      }
    }
  }
}
