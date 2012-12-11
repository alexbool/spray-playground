package com.alexb.swift

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.client.HttpConduit
import HttpConduit._

class SwiftClient(authUrl: String,
                  credentials: SwiftCredentials,
                  httpClient: ActorRef)
  extends Actor with ActorLogging {

  private var authentication: Option[AuthenticationResult] = None

  private def getToken = {
    if (authentication.isEmpty) authentication = authenticate().toOption
    authentication.get.token
  }

  private def authenticate() = {
    val authenticator = context.system.actorOf(Props(new Authenticator(authUrl, httpClient)))
    val authFuture = (authenticator ? Authenticate(credentials)).mapTo[AuthenticationResult]
    Await.ready(authFuture, 10 seconds)
    context.stop(authenticator)
    authFuture.value.get
  }

  def receive = ???
}
