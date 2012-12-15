package com.alexb.swift

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.client.HttpConduit._

private[swift] trait ObjectActions extends SwiftApiUtils {
  def getObject(rootPath: String, container: String, `object`: String, token: String, httpConduit: ActorRef)(implicit ctx: ExecutionContext) =
    Get(mkUrl(rootPath, container, `object`)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit)
    ) map { resp =>
      Object(`object`, resp.entity.buffer)
    }
}