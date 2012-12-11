package com.alexb.swift

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import spray.client.HttpConduit
import spray.client.HttpConduit._

case class SwiftCredentials(user: String, key: String)

// Messages
private[swift] case class Authenticate(credentials: SwiftCredentials)
private[swift] case class AuthenticationResult(token: String, storageUrl: String)

private[swift] class Authenticator(authUrl: String,
                                   httpClient: ActorRef)
  extends Actor with ActorLogging {

  private val conduit = context.system.actorOf(Props(new HttpConduit(httpClient, authUrl, 80)))

  private def authPipeline(credentials: SwiftCredentials) =
    addHeader("X-Auth-User", credentials.user) ~>
    addHeader("X-Auth-Key", credentials.key) ~>
    sendReceive(conduit)

  def receive = {
    case msg: Authenticate => {
      val result = (Get("/v1.0") ~> authPipeline(msg.credentials))
        .map { resp =>
          AuthenticationResult(
            resp.headers.find(_.name == "X-Auth-Token").map(_.value).get,
            resp.headers.find(_.name == "X-Storage-Url").map(_.value).get)
        }
      sender ! result
    }
  }
}
