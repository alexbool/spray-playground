package com.alexb.swift

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import akka.pattern.ask
import scala.concurrent.Future
import scala.util.Try
import spray.client.HttpConduit

class SwiftClient(authUrl: String,
                  credentials: SwiftCredentials,
                  httpClient: ActorRef)
  extends Actor with ActorLogging
  with AccountActions {

  private var authentication: Option[Try[AuthenticationResult]] = None
  private var httpConduit: Option[ActorRef] = None

  def receive = {
    case msg: ListContainers => authResult().map()
  }

  private def authResult() =
    if (authentication.isEmpty || authentication.get.isFailure) {
      val future = authenticate
      future onComplete { res =>
        authentication = Some(res)
      }
      future
    } else Future.successful(authentication.get.get)

  private def authenticate = {
    val authenticator = context.system.actorOf(Props(new Authenticator(authUrl, httpClient)))
    val authFuture = (authenticator ? Authenticate(credentials)).mapTo[AuthenticationResult]
    context.stop(authenticator)
    authFuture
  }
}
