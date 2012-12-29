package com.alexb.swift

import spray.client.HttpConduit
import spray.client.HttpConduit._
import akka.actor.{Props, Actor, ActorRef, ActorLogging}
import scala.concurrent.ExecutionContext

trait AuthenticationActions {
  this: Actor with ActorLogging =>

  def authenticate(httpClient: ActorRef,
                   credentials: SwiftCredentials,
                   host: String,
                   port: Int,
                   sslEnabled: Boolean)(implicit ctx: ExecutionContext) = {
    val authConduit = context.actorOf(
      props = Props(new HttpConduit(httpClient, host, port, sslEnabled)),
      name = "auth-http-conduit")
    log.debug(s"About to make authentication request: $credentials")
    val auth = (Get("/v1.0") ~> authPipeline(authConduit, credentials))
      .map { resp =>
      log.debug(s"Recieved authentication response: $resp")
      AuthenticationResult(
        resp.headers.find(_.is("x-auth-token")).map(_.value).get,
        resp.headers.find(_.is("x-storage-url")).map(h => Url(h.value)).get)
    }
    auth onComplete { r =>
      context.stop(authConduit)
    }
    auth
  }

  private def authPipeline(conduit: ActorRef, credentials: SwiftCredentials) =
    addHeader("X-Auth-User", credentials.user) ~>
      addHeader("X-Auth-Key", credentials.key) ~>
      sendReceive(conduit)
}
