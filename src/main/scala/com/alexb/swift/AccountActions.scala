package com.alexb.swift

import akka.actor.ActorRefFactory
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import spray.client.pipelining._

private[swift] trait AccountActions extends SwiftApiUtils with SwiftMarshallers {
  def listContainers(rootPath: String, token: String)(implicit refFactory: ActorRefFactory,
                                                      ctx: ExecutionContext, futureTimeout: Timeout) =
    Get(mkUrlJson(rootPath)) ~> (
      authHeader(token) ~>
      sendReceive(refFactory, ctx, futureTimeout) ~>
      unmarshal[Seq[Container]]
    )
}
