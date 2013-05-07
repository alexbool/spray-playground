package com.alexb.swift

import akka.actor.{ActorRefFactory, Actor, ActorLogging}
import akka.util.Timeout
import scala.concurrent.{Future, ExecutionContext}
import spray.client.pipelining._

trait SwiftAuthentication {
  this: Actor with ActorLogging =>

  def authenticate(credentials: Credentials,
                   authUrl: String)
                  (implicit refFactory: ActorRefFactory,
                   ctx: ExecutionContext, futureTimeout: Timeout): Future[AuthenticationResult] = {
    log.debug(s"About to make authentication request: $credentials")
    (Get(authUrl) ~> authPipeline(credentials))
    .map { resp =>
      log.debug(s"Recieved authentication response: $resp")
      AuthenticationResult(
        resp.headers.find(_.is("x-auth-token")).map(_.value).get,
        resp.headers.find(_.is("x-storage-url")).map(_.value).get)
    }
  }

  private def authPipeline(credentials: Credentials)
                          (implicit refFactory: ActorRefFactory,
                           ctx: ExecutionContext, futureTimeout: Timeout) =
    addHeader("X-Auth-User", credentials.user) ~>
    addHeader("X-Auth-Key", credentials.key) ~>
    sendReceive(refFactory, ctx, futureTimeout)
}
