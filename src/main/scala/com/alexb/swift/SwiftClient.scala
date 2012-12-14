package com.alexb.swift

import akka.actor.{ActorRef, ActorLogging, Props, Actor}
import akka.pattern.{ask, pipe}
import scala.concurrent.duration._
import scala.concurrent.Future
import spray.client.HttpConduit
import akka.util.Timeout
import spray.can.client.HttpClient
import spray.io.IOExtension

class SwiftClient(authHost: String,
                  authPort: Int = 80,
                  authSslEnabled: Boolean = false,
                  credentials: SwiftCredentials)
  extends Actor with ActorLogging
  with AccountActions with ContainerActions {

  implicit val timeout = Timeout(10 seconds)
  implicit val ctx = context.dispatcher

  private val httpClient = context.actorOf(Props(new HttpClient(IOExtension(context.system).ioBridge)))
  private val authenticator = context.actorOf(Props(new Authenticator(httpClient, authHost, authPort, authSslEnabled)))

  def receive = {
    case ListContainers =>
      executeRequest((auth, conduit) => listContainers(auth.storageUrl.path, auth.token, conduit))

    case ListObjects(container) =>
      executeRequest((auth, conduit) => listObjects(auth.storageUrl.path, container, auth.token, conduit))

    case CreateContainer(container) =>
      executeRequest((auth, conduit) => createContainer(auth.storageUrl.path, container, auth.token, conduit))
  }

  private def authentication = (authenticator ? Authenticate(credentials)).mapTo[AuthenticationResult]

  private def executeRequest[R](f: (AuthenticationResult, ActorRef) => Future[R]) {
    authentication
    .flatMap(auth => {
      val conduit = context.actorOf(Props(
        new HttpConduit(httpClient, auth.storageUrl.host, auth.storageUrl.port, auth.storageUrl.sslEnabled)))
      val result = f(auth, conduit)
      result onComplete { r =>
        context.stop(conduit)
      }
      result
    })
    .pipeTo(sender)
  }
}
