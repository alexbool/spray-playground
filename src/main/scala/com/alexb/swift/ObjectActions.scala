package com.alexb.swift

import akka.actor.ActorRefFactory
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import spray.client.pipelining._
import spray.http.{HttpEntity, StatusCodes, MediaType}
import spray.http.HttpHeaders.`Content-Type`

private[swift] trait ObjectActions extends SwiftApiUtils {
  def getObject(rootPath: String,
                container: String,
                `object`: String,
                token: String)
               (implicit refFactory: ActorRefFactory, ctx: ExecutionContext, futureTimeout: Timeout) =
    Get(mkUrl(rootPath, container, `object`)) ~> (
      authHeader(token) ~>
      sendReceive(refFactory, ctx, futureTimeout)
    ) map { resp =>
      if (resp.status.isSuccess)
        Some(Object(`object`, resp.header[`Content-Type`].get.contentType.mediaType, resp.entity.buffer))
      else None
    }

  def putObject(rootPath: String,
                container: String,
                `object`: String,
                mediaType: MediaType,
                data: Array[Byte],
                token: String)
               (implicit refFactory: ActorRefFactory, ctx: ExecutionContext, futureTimeout: Timeout) =
    Put(mkUrl(rootPath, container, `object`), HttpEntity(mediaType, data)) ~> (
      authHeader(token) ~>
      sendReceive(refFactory, ctx, futureTimeout)
    ) map { resp =>
      PutObjectResult(resp.status.isSuccess)
    }

  def deleteObject(rootPath: String,
                   container: String,
                   `object`: String,
                   token: String)
                  (implicit refFactory: ActorRefFactory, ctx: ExecutionContext, futureTimeout: Timeout) =
    Delete(mkUrl(rootPath, container, `object`)) ~> (
      authHeader(token) ~>
      sendReceive(refFactory, ctx, futureTimeout)
    ) map { resp =>
      DeleteObjectResult(
        resp.status.isSuccess || resp.status == StatusCodes.NotFound,
        resp.status == StatusCodes.NotFound)
    }
}
