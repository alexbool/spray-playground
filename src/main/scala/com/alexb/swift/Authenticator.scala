package com.alexb.swift

import akka.actor.{ActorLogging, Actor}
import akka.util.Timeout
import spray.httpx.UnsuccessfulResponseException
import spray.http.StatusCodes
import scala.util.control.NonFatal

private[swift] class Authenticator(credentials: Credentials, authUrl: String)(implicit futureTimeout: Timeout)
  extends Actor with Authentication with ActorLogging {

  import context.dispatcher

  val revisionCounter = Iterator from 0
  var currentRevision = 0

  def receive = {
    case TryAuthenticate            => tryAuthenticate()
    case AuthenticationExpired(rev) => if (rev == currentRevision) tryAuthenticate()
  }

  def tryAuthenticate() {
    log.debug(s"About to refresh authentication. Current revision: $currentRevision")
    currentRevision = revisionCounter.next()
    val resultF = authenticate(credentials, authUrl, currentRevision)
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
