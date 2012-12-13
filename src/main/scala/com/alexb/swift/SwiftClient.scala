package com.alexb.swift

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import akka.pattern.{ask, pipe}
import scala.concurrent.duration._
import spray.client.HttpConduit
import akka.util.Timeout

class SwiftClient(authUrl: String,
                  authPort: Int = 80,
                  authSslEnabled: Boolean = false,
                  credentials: SwiftCredentials,
                  httpClient: ActorRef)
  extends Actor with ActorLogging
  with AccountActions {

  implicit val timeout = Timeout(10 seconds)
  implicit val ctx = context.dispatcher
  private val authenticator = context.actorOf(Props(new Authenticator(authUrl, authPort, authSslEnabled, httpClient)))

  def receive = {
    case ListContainers =>
      authentication
      .flatMap(auth => {
        val conduit = context.actorOf(Props(
          new HttpConduit(httpClient, auth.storageUrl.host, auth.storageUrl.port, auth.storageUrl.sslEnabled)))
        val result = listContainers(auth.storageUrl.path, credentials.user, auth.token, conduit)
        context.stop(conduit)
        result
      })
      .pipeTo(sender)
  }

  private def authentication = (authenticator ? Authenticate(credentials)).mapTo[AuthenticationResult]
}
