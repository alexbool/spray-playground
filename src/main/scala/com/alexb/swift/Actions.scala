package com.alexb.swift

import spray.client.pipelining._
import spray.http.{HttpEntity, MediaType, StatusCodes, HttpRequest, HttpResponse}
import spray.http.HttpHeaders.`Content-Type`

import SwiftApiUtils._
import Marshallers._

private[swift] sealed trait Action[R] {
  def buildRequest(auth: AuthenticationResult): HttpRequest
  def parseResponse(resp: HttpResponse): R
}

private[swift] class ListContainersAction extends Action[Seq[Container]] {
  def buildRequest(auth: AuthenticationResult) = Get(mkUrlJson(auth.storageUrl)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) = unmarshal[Seq[Container]].apply(resp)
}

private[swift] class ListObjectsAction(request: ListObjects) extends Action[Seq[ObjectMetadata]] {
  def buildRequest(auth: AuthenticationResult) = Get(mkUrlJson(auth.storageUrl, request.container)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) = unmarshal[Seq[ObjectMetadata]].apply(resp)
}

private[swift] class CreateContainerAction(request: CreateContainer) extends Action[CreateContainerResult] {
  def buildRequest(auth: AuthenticationResult) = Put(mkUrl(auth.storageUrl, request.container)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) = CreateContainerResult(resp.status.isSuccess, resp.status == StatusCodes.Accepted)
}

private[swift] class DeleteContainerAction(request: DeleteContainer) extends Action[DeleteContainerResult] {
  def buildRequest(auth: AuthenticationResult) = Delete(mkUrl(auth.storageUrl, request.container)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) =
    DeleteContainerResult(
      resp.status.isSuccess || resp.status == StatusCodes.NotFound,
      resp.status == StatusCodes.NotFound)
}

private[swift] class GetObjectAction(request: GetObject) extends Action[Option[Object]] {
  def buildRequest(auth: AuthenticationResult) =
    Get(mkUrl(auth.storageUrl, request.container, request.name)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) =
    if (resp.status.isSuccess)
      Some(Object(request.name, resp.header[`Content-Type`].get.contentType.mediaType, resp.entity.data.toByteArray))
    else None
}

private[swift] class PutObjectAction(request: PutObject) extends Action[PutObjectResult] {
  def buildRequest(auth: AuthenticationResult) =
    Put(mkUrl(auth.storageUrl, request.container, request.name), HttpEntity(request.mediaType, request.data)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) = PutObjectResult(resp.status.isSuccess)
}

private[swift] class DeleteObjectAction(request: DeleteObject) extends Action[DeleteObjectResult] {
  def buildRequest(auth: AuthenticationResult) =
    Delete(mkUrl(auth.storageUrl, request.container, request.name)) ~> authHeader(auth.token)
  def parseResponse(resp: HttpResponse) =
    DeleteObjectResult(
      resp.status.isSuccess || resp.status == StatusCodes.NotFound,
      resp.status == StatusCodes.NotFound)
}
