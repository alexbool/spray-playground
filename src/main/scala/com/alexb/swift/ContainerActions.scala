package com.alexb.swift

import akka.actor.ActorRefFactory
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import spray.client.pipelining._
import spray.http.StatusCodes

private[swift] trait ContainerActions extends SwiftApiUtils with SwiftMarshallers {
  def listObjects(rootPath: String,
                  container: String,
                  token: String)
                 (implicit refFactory: ActorRefFactory, ctx: ExecutionContext, futureTimeout: Timeout) =
    Get(mkUrlJson(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(refFactory, ctx, futureTimeout) ~>
      unmarshal[Seq[ObjectMetadata]]
    )

  def createContainer(rootPath: String,
                      container: String,
                      token: String)
                     (implicit refFactory: ActorRefFactory, ctx: ExecutionContext, futureTimeout: Timeout) =
    Put(mkUrl(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(refFactory, ctx, futureTimeout)
    ) map { resp =>
      CreateContainerResult(resp.status.isSuccess, resp.status == StatusCodes.Accepted)
    }

  def deleteContainer(rootPath: String,
                      container: String,
                      token: String)
                     (implicit refFactory: ActorRefFactory, ctx: ExecutionContext, futureTimeout: Timeout) =
    Delete(mkUrl(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(refFactory, ctx, futureTimeout)
    ) map { resp =>
      DeleteContainerResult(
        resp.status.isSuccess || resp.status == StatusCodes.NotFound,
        resp.status == StatusCodes.NotFound)
    }
}
