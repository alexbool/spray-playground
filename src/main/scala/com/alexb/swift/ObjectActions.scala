package com.alexb.swift

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.client.HttpConduit._
import spray.http.{StatusCodes, MediaType, HttpBody}
import spray.http.HttpHeaders.`Content-Type`

private[swift] trait ObjectActions extends SwiftApiUtils {
  def getObject(rootPath: String,
                container: String,
                `object`: String,
                token: String,
                httpConduit: ActorRef)
               (implicit ctx: ExecutionContext) =
    Get(mkUrl(rootPath, container, `object`)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit)
    ) map { resp =>
      Object(`object`, resp.header[`Content-Type`].get.contentType.mediaType, resp.entity.buffer)
    }

  def putObject(rootPath: String,
                container: String,
                `object`: String,
                mediaType: MediaType,
                data: Array[Byte],
                token: String,
                httpConduit: ActorRef)
               (implicit ctx: ExecutionContext) =
    Put(mkUrl(rootPath, container, `object`), HttpBody(mediaType, data)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit)
    ) map { resp =>
      CreateObjectResult(resp.status.isSuccess)
    }

  def deleteObject(rootPath: String,
                   container: String,
                   `object`: String,
                   token: String,
                   httpConduit: ActorRef)
                  (implicit ctx: ExecutionContext) =
    Delete(mkUrl(rootPath, container, `object`)) ~> (
      authHeader(token) ~>
      sendReceive(httpConduit)
    ) map { resp =>
      DeleteObjectResult(
        resp.status.isSuccess || resp.status == StatusCodes.NotFound,
        resp.status == StatusCodes.NotFound)
    }
}
