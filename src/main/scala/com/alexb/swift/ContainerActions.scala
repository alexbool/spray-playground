package com.alexb.swift

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.client.pipelining._
import spray.http.StatusCodes

private[swift] trait ContainerActions extends SwiftApiUtils with SwiftMarshallers {
  def listObjects(rootPath: String,
                  container: String,
                  token: String,
                  httpClient: ActorRef)
                 (implicit ctx: ExecutionContext) =
    Get(mkUrlJson(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(httpClient) ~>
      unmarshal[Seq[ObjectMetadata]]
    )

  def createContainer(rootPath: String,
                      container: String,
                      token: String,
                      httpClient: ActorRef)
                     (implicit ctx: ExecutionContext) =
    Put(mkUrl(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(httpClient)
    ) map { resp =>
      CreateContainerResult(resp.status.isSuccess, resp.status == StatusCodes.Accepted)
    }

  def deleteContainer(rootPath: String,
                      container: String,
                      token: String,
                      httpClient: ActorRef)
                     (implicit ctx: ExecutionContext) =
    Delete(mkUrl(rootPath, container)) ~> (
      authHeader(token) ~>
      sendReceive(httpClient)
    ) map { resp =>
      DeleteContainerResult(
        resp.status.isSuccess || resp.status == StatusCodes.NotFound,
        resp.status == StatusCodes.NotFound)
    }
}
