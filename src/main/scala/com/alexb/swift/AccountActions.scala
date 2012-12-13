package com.alexb.swift

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.client.HttpConduit._

case class Container(name: String, count: Int, bytes: Long)

private[swift] trait AccountActions extends SwiftApiUtils with SwiftApiMarshallers {
  def listContainers(rootPath: String, account: String, token: String, httpConduit: ActorRef)(implicit ctx: ExecutionContext) =
    Get(accountUrl(rootPath, account)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit) ~>
      unmarshal[Seq[Container]]
    )
}
