package com.alexb.swift

import spray.routing.{RequestContext, HttpService}
import spray.http.{EmptyEntity, HttpResponse, StatusCodes}
import spray.http.HttpHeaders.RawHeader
import collection.convert.Wrappers.JSetWrapper

trait MockSwiftServer extends HttpService with SwiftApiMarshallers {

  def port: Int
  def token: String

  val containers = new java.util.concurrent.CopyOnWriteArraySet[Container]

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
              RawHeader("X-Storage-Url", s"http://$host:$port/v1/rootpath") :: RawHeader("X-Auth-Token", token) :: Nil))
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
            JSetWrapper(containers).to[Seq]
          }
        }
      } ~
      path(PathElement) { container =>
        put {
          val alreadyExists = containers.add(Container(container, 0, 0))
          complete(if (alreadyExists) StatusCodes.Accepted else StatusCodes.Created)
        }
      }
    }
}
