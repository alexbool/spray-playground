package com.alexb.swift

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.actor.Status.Failure
import scala.concurrent.Future
import scala.util.control.NonFatal
import spray.httpx.UnsuccessfulResponseException
import spray.http.StatusCodes

private[swift] class Worker[R](action: Action[R], recipient: ActorRef)
  extends Actor with ActorLogging {

  case object Retry

  import context.dispatcher

  var inProgress = false
  var fresherAuth: Option[AuthenticationResult] = None

  def receive = {
    case GotAuthentication(auth) => handleGotAuthentication(auth)
    case Retry                   => handleRetry()
    case BadCredentials          => recipient ! Failure(new BadCredentialsException)
    case AuthenticationFailed(e) => recipient ! Failure(new SwiftException(e))
  }

  def handleGotAuthentication(auth: AuthenticationResult) {
    log.debug(s"Got authentication: $auth")
    if (!inProgress)
      executeAction(auth)
    else
      fresherAuth = Some(auth)
  }

  def handleRetry() {
    fresherAuth match {
      case Some(auth) => executeAction(auth)
      case None       => inProgress = false
    }
  }

  def executeAction(auth: AuthenticationResult) {
    log.debug("Executing action")
    inProgress = true
    val resultF: Future[R] = action(auth)
    resultF onSuccess { case result =>
      log.debug(s"Successful result: $result, stopping")
      recipient ! result
      context.stop(self)
    }
    resultF onFailure {
      case e: UnsuccessfulResponseException if e.response.status == StatusCodes.Unauthorized => {
        // Notify that current authentication expired, wait for another GotAuthentication message
        context.parent ! AuthenticationExpired(auth.revision)
        self ! Retry
        log.info("Got 401 response, will wait for new authentication to retry")
      }
      case NonFatal(e) => {
        log.warning(s"Error when performing request: $e, stopping")
        recipient ! Failure(new SwiftException(e))
        context.stop(self)
      }
    }
  }
}
