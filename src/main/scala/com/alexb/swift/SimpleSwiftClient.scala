package com.alexb.swift

import akka.actor.{ActorRefFactory, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.http.MediaType
import language.postfixOps

class SimpleSwiftClient(credentials: Credentials, authUrl: String)
                       (implicit refFactory: ActorRefFactory, timeout: Timeout = 20 seconds) {

  private val client = refFactory.actorOf(props = Props(new SwiftClient(credentials, authUrl)))

  def listContainers = (client ? ListContainers).mapTo[Seq[Container]]
  def listObjects(container: String) = (client ? ListObjects(container)).mapTo[Seq[ObjectMetadata]]
  def createContainer(container: String) = (client ? CreateContainer(container)).mapTo[CreateContainerResult]
  def deleteContainer(container: String) = (client ? DeleteContainer(container)).mapTo[DeleteContainerResult]
  def getObject(container: String, name: String) = (client ? GetObject(container, name)).mapTo[Option[Object]]
  def putObject(container: String, name: String, mediaType: MediaType, data: Array[Byte]) = (client ? PutObject(container, name, mediaType, data)).mapTo[PutObjectResult]
  def putObject(container: String, name: String, mediaType: String, data: Array[Byte]) = (client ? PutObject(container, name, mediaType, data)).mapTo[PutObjectResult]
  def putObject(container: String, name: String, data: Array[Byte]) = (client ? PutObject(container, name, data)).mapTo[PutObjectResult]
  def deleteObject(container: String, name: String) = (client ? DeleteObject(container, name)).mapTo[DeleteObjectResult]
}
