package com.alexb.swift

import spray.client.HttpConduit._
import akka.actor.{ActorRef, ActorLogging}
import scala.concurrent.ExecutionContext

trait AuthenticationActions {
  this: ActorLogging =>

  def authenticate(conduit: ActorRef, msg: Authenticate)(implicit ctx: ExecutionContext) = {
    log.debug(s"About to make authentication request: $msg")
    (Get("/v1.0") ~> authPipeline(conduit, msg.credentials))
      .map { resp =>
      log.debug(s"Recieved authentication response: $resp")
      AuthenticationResult(
        resp.headers.find(_.is("x-auth-token")).map(_.value).get,
        resp.headers.find(_.is("x-storage-url")).map(h => Url(h.value)).get)
    }
  }

  private def authPipeline(conduit: ActorRef, credentials: SwiftCredentials) =
    addHeader("X-Auth-User", credentials.user) ~>
      addHeader("X-Auth-Key", credentials.key) ~>
      sendReceive(conduit)
}
