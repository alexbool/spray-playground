package com.alexb.swift

import spray.client.pipelining._
import spray.http.{HttpEntity, MediaType, StatusCodes, HttpRequest, HttpResponse}
import spray.http.HttpHeaders.`Content-Type`

import SwiftApiUtils._
import Marshallers._

private[swift] trait Action[R] {
  def buildRequest(auth: AuthenticationResult): HttpRequest
  def parseResponse(resp: HttpResponse): R
}

private[swift] class ListContainersAction extends Action[Seq[Container]] {
  def buildRequest(auth: AuthenticationResult) = Get(mkUrlJson(auth.storageUrl)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) = unmarshal[Seq[Container]].apply(resp)
}

private[swift] class ListObjectsAction(container: String) extends Action[Seq[ObjectMetadata]] {
  def buildRequest(auth: AuthenticationResult) = Get(mkUrlJson(auth.storageUrl, container)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) = unmarshal[Seq[ObjectMetadata]].apply(resp)
}

private[swift] class CreateContainerAction(container: String) extends Action[CreateContainerResult] {
  def buildRequest(auth: AuthenticationResult) = Put(mkUrl(auth.storageUrl, container)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) = CreateContainerResult(resp.status.isSuccess, resp.status == StatusCodes.Accepted)
}

private[swift] class DeleteContainerAction(container: String) extends Action[DeleteContainerResult] {
  def buildRequest(auth: AuthenticationResult) = Delete(mkUrl(auth.storageUrl, container)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) =
    DeleteContainerResult(
      resp.status.isSuccess || resp.status == StatusCodes.NotFound,
      resp.status == StatusCodes.NotFound)
}

private[swift] class GetObjectAction(container: String, `object`: String) extends Action[Option[Object]] {
  def buildRequest(auth: AuthenticationResult) =
    Get(mkUrl(auth.storageUrl, container, `object`)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) =
    if (resp.status.isSuccess)
      Some(Object(`object`, resp.header[`Content-Type`].get.contentType.mediaType, resp.entity.buffer))
    else None
}

private[swift] class PutObjectAction(container: String, `object`: String, mediaType: MediaType, data: Array[Byte])
  extends Action[PutObjectResult] {

  def buildRequest(auth: AuthenticationResult) =
    Put(mkUrl(auth.storageUrl, container, `object`), HttpEntity(mediaType, data)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) = PutObjectResult(resp.status.isSuccess)
}

private[swift] class DeleteObjectAction(container: String, `object`: String) extends Action[DeleteObjectResult] {
  def buildRequest(auth: AuthenticationResult) =
    Delete(mkUrl(auth.storageUrl, container, `object`)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) =
    DeleteObjectResult(
      resp.status.isSuccess || resp.status == StatusCodes.NotFound,
      resp.status == StatusCodes.NotFound)
}
