package com.alexb.swift

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.client.HttpConduit._
import spray.http.StatusCodes

private[swift] trait ContainerActions extends SwiftApiUtils with SwiftApiMarshallers {
  def listObjects(rootPath: String,
                  container: String,
                  token: String,
                  httpConduit: ActorRef)
                 (implicit ctx: ExecutionContext) =
    Get(mkUrlJson(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit) ~>
      unmarshal[Seq[ObjectMetadata]]
    )

  def createContainer(rootPath: String,
                      container: String,
                      token: String,
                      httpConduit: ActorRef)
                     (implicit ctx: ExecutionContext) =
    Put(mkUrl(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit)
    ) map { resp =>
      CreateContainerResult(resp.status.isSuccess, resp.status == StatusCodes.Accepted)
    }

  def deleteContainer(rootPath: String,
                      container: String,
                      token: String,
                      httpConduit: ActorRef)
                     (implicit ctx: ExecutionContext) =
    Delete(mkUrl(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit)
    ) map { resp =>
      DeleteContainerResult(
        resp.status.isSuccess || resp.status == StatusCodes.NotFound,
        resp.status == StatusCodes.NotFound)
    }
}
