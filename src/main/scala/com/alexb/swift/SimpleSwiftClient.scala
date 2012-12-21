package com.alexb.swift

import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.http.MediaType

class SimpleSwiftClient(credentials: SwiftCredentials,
                        authHost: String,
                        authPort: Int = 80,
                        authSslEnabled: Boolean = false)
                       (implicit system: ActorSystem, timeout: Timeout = Timeout(20 seconds)) {

  private val client = system.actorOf(props = Props(new SwiftClient(credentials, authHost, authPort, authSslEnabled)))

  def listContainers = (client ? ListContainers).mapTo[Seq[Container]]
  def listObjects(container: String) = (client ? ListObjects(container)).mapTo[Seq[ObjectMetadata]]
  def createContainer(container: String) = (client ? CreateContainer(container)).mapTo[CreateContainerResult]
  def deleteContainer(container: String) = (client ? DeleteContainer(container)).mapTo[DeleteContainerResult]
  def getObject(container: String, name: String) = (client ? GetObject(container, name)).mapTo[Object]
  def putObject(container: String, name: String, mediaType: MediaType, data: Array[Byte]) = (client ? PutObject(container, name, mediaType, data)).mapTo[PutObjectResult]
  def putObject(container: String, name: String, mediaType: String, data: Array[Byte]) = (client ? PutObject(container, name, mediaType, data)).mapTo[PutObjectResult]
  def putObject(container: String, name: String, data: Array[Byte]) = (client ? PutObject(container, name, data)).mapTo[PutObjectResult]
  def deleteObject(container: String, name: String) = (client ? DeleteObject(container, name)).mapTo[DeleteObjectResult]
}
