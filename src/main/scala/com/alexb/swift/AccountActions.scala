package com.alexb.swift

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.client.HttpConduit._

private[swift] trait AccountActions extends SwiftApiUtils with SwiftMarshallers {
  def listContainers(rootPath: String, token: String, httpConduit: ActorRef)(implicit ctx: ExecutionContext) =
    Get(mkUrlJson(rootPath)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit) ~>
      unmarshal[Seq[Container]]
    )
}
