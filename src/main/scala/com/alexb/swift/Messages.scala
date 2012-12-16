package com.alexb.swift

// External API messages
case object ListContainers

case class ListObjects(container: String)
case class CreateContainer(container: String)
case class DeleteContainer(container: String)

case class GetObject(container: String, name: String)
case class PutObject(container: String, name: String, data: Array[Byte])
case class DeleteObject(container: String, name: String)

case class OperationResult(success: Boolean)
case class DeleteResult(success: Boolean, alreadyDeleted: Boolean)
