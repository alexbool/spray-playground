package com.alexb.swift

import spray.http.{MediaTypes, MediaType}

///////////////////////////// External API messages /////////////////////////////

// Command messages
sealed trait Request

case object ListContainers extends Request

case class ListObjects(container: String) extends Request
case class CreateContainer(container: String) extends Request
case class DeleteContainer(container: String) extends Request

case class GetObject(container: String, name: String) extends Request
case class PutObject(container: String, name: String, mediaType: MediaType, data: Array[Byte]) extends Request {
  def this(container: String, name: String, data: Array[Byte]) =
    this(container, name, MediaTypes.forExtension(name.split('.').last).get, data)

  def this(container: String, name: String, mediaType: String, data: Array[Byte]) =
    this(container, name, MediaType.custom(mediaType), data)
}

object PutObject {
  def apply(container: String, name: String, data: Array[Byte]) =
    new PutObject(container, name, data)

  def apply(container: String, name: String, mediaType: String, data: Array[Byte]) =
    new PutObject(container, name, mediaType, data)
}

case class DeleteObject(container: String, name: String) extends Request

// Response messages
case class CreateContainerResult(success: Boolean, alreadyExists: Boolean)
case class DeleteContainerResult(success: Boolean, alreadyDeleted: Boolean)

case class PutObjectResult(success: Boolean)
case class DeleteObjectResult(success: Boolean, alreadyDeleted: Boolean)

/////////////////////////////// Internal messages ///////////////////////////////
private[swift] case class GotAuthentication(auth: AuthenticationResult)
private[swift] case class AuthenticationExpired(revision: Int)
private[swift] case object TryAuthenticate
private[swift] case class AuthenticationFailed(cause: Throwable)
private[swift] case object BadCredentials
