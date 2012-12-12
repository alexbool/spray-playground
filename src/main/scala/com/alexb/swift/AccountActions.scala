package com.alexb.swift

import akka.actor.ActorRef
import spray.client.HttpConduit._

case class Container(name: String, count: Int, bytes: Long)

private[swift] trait AccountActions extends SwiftApiUtils with SwiftApiMarshallers {
  def listContainers(account: String, token: String, httpConduit: ActorRef) =
    Get(accountUrl(account)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit) ~>
      unmarshal[Seq[Container]]
    )
}
