package com.alexb.swift

import spray.routing.{RequestContext, HttpService}
import spray.http.{EmptyEntity, HttpResponse, StatusCodes}
import spray.http.HttpHeaders.RawHeader
import akka.actor.Actor
import collection.concurrent.TrieMap
import org.joda.time.Instant

class MockSwiftServer extends Actor with HttpService with SwiftApiMarshallers {

  val token = "some_token"

  val containers = TrieMap[Container, TrieMap[String, Object]]()

  def receive = runRoute(authRoute ~ storageRoute)
  def actorRefFactory = context.system

  val authRoute =
    path("v1.0") {
      get { ctx: RequestContext =>
        if (ctx.request.headers.find(_.is("x-auth-user")).isDefined &&
          ctx.request.headers.find(_.is("x-auth-key")).isDefined) {
          val host = ctx.request.headers.find(_.is("host")).get.value
          ctx.complete(
            HttpResponse(
              StatusCodes.OK,
              EmptyEntity,
              RawHeader("X-Storage-Url", s"http://$host/v1/rootpath") :: RawHeader("X-Auth-Token", token) :: Nil))
        } else {
          ctx.complete(StatusCodes.Unauthorized)
        }
      }
    }

  val storageRoute =
    pathPrefix("v1/rootpath") {
      path("") {
        get {
          complete {
            containers.keys
          }
        }
      } ~
      path(PathElement) { container =>
        put {
          val alreadyExists = containers.putIfAbsent(Container(container, 0, 0), TrieMap[String, Object]()).isDefined
          complete(if (alreadyExists) StatusCodes.Accepted else StatusCodes.Created)
        } ~
        get {
          complete {
            containers.find(_._1.name == container)
              .map(_._2.values)
              .getOrElse(Iterable[Object]())
              .map(o => ObjectMetadata(o.name, "some_hash", 100, "text/plain", new Instant))
          }
        }
      }
    }
}
