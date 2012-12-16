package com.alexb.swift

import spray.http.{MediaTypes, MediaType}
import spray.http.MediaTypes.CustomMediaType

///////////////////////////// External API messages /////////////////////////////

// Command messages
case object ListContainers

case class ListObjects(container: String)
case class CreateContainer(container: String)
case class DeleteContainer(container: String)

case class GetObject(container: String, name: String)
case class PutObject(container: String, name: String, mediaType: MediaType, data: Array[Byte]) {
  def this(container: String, name: String, data: Array[Byte]) =
    this(container, name, MediaTypes.forExtension(name.split('.').last).get, data)

  def this(container: String, name: String, mediaType: String, data: Array[Byte]) =
    this(container, name, CustomMediaType(mediaType), data)
}

object PutObject {
  def apply(container: String, name: String, data: Array[Byte]) =
    new PutObject(container, name, data)

  def apply(container: String, name: String, mediaType: String, data: Array[Byte]) =
    new PutObject(container, name, mediaType, data)
}

// Response messages
case class DeleteObject(container: String, name: String)

case class CreateContainerResult(success: Boolean, alreadyExists: Boolean)
case class DeleteContainerResult(success: Boolean, alreadyDeleted: Boolean)

case class CreateObjectResult(success: Boolean)
