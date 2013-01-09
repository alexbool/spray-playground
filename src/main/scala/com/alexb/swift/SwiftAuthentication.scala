package com.alexb.swift

import akka.actor.{Actor, ActorRef, ActorLogging}
import scala.concurrent.ExecutionContext
import spray.client.pipelining._

trait SwiftAuthentication {
  this: Actor with ActorLogging =>

  def authenticate(httpClient: ActorRef,
                   credentials: SwiftCredentials,
                   host: String,
                   port: Int,
                   sslEnabled: Boolean)(implicit ctx: ExecutionContext) = {
    log.debug(s"About to make authentication request: $credentials")
    (Get(Url(host, port, sslEnabled, "/v1.0").toString) ~> authPipeline(httpClient, credentials))
      .map { resp =>
      log.debug(s"Recieved authentication response: $resp")
      AuthenticationResult(
        resp.headers.find(_.is("x-auth-token")).map(_.value).get,
        resp.headers.find(_.is("x-storage-url")).map(h => Url(h.value)).get)
    }
  }

  private def authPipeline(httpClient: ActorRef, credentials: SwiftCredentials) =
    addHeader("X-Auth-User", credentials.user) ~>
      addHeader("X-Auth-Key", credentials.key) ~>
      sendReceive(httpClient)(context.dispatcher)
}
