package com.alexb.swift

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.client.HttpConduit._

private[swift] trait ContainerActions extends SwiftApiUtils with SwiftApiMarshallers {
  def listObjects(rootPath: String, container: String, token: String, httpConduit: ActorRef)(implicit ctx: ExecutionContext) =
    Get(mkUrlJson(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit) ~>
      unmarshal[Seq[ObjectMetadata]]
    )

  def createContainer(rootPath: String, container: String, token: String, httpConduit: ActorRef)(implicit ctx: ExecutionContext) =
    Put(mkUrl(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit)
    ) map { resp =>
      OperationResult(resp.status.isSuccess)
    }
}