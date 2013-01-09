package com.alexb.swift

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.client.pipelining._

private[swift] trait AccountActions extends SwiftApiUtils with SwiftMarshallers {
  def listContainers(rootPath: String, token: String, httpClient: ActorRef)(implicit ctx: ExecutionContext) =
    Get(mkUrlJson(rootPath)) ~> (
      authHeader(token) ~>
      sendReceive(httpClient) ~>
      unmarshal[Seq[Container]]
    )
}
