package com.alexb.swift

import akka.actor.{ActorLogging, Actor}
import spray.routing.HttpService
import spray.http._
import spray.http.HttpHeaders.{`Content-Length`, `Content-Type`}
import spray.routing.authentication.HttpAuthenticator
import spray.routing.RequestContext
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpResponse
import collection.concurrent.TrieMap
import org.joda.time.Instant
import java.security.MessageDigest
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

case object RegenerateToken

class StubSwiftServer extends Actor with HttpService with SwiftMarshallers with ActorLogging {

  var token = generateToken

  val containers = TrieMap[Container, TrieMap[String, (ObjectMetadata, Object)]]()

  def receive = handleRegenerateToken orElse runRoute(authRoute ~ storageRoute)
  def actorRefFactory = context.system

  implicit val ec: ExecutionContext = context.dispatcher

  def generateToken = {
    val newToken = Random.alphanumeric.take(32).mkString
    log.debug(s"Generated new token: $newToken")
    newToken
  }

  def handleRegenerateToken: Actor.Receive = {
    case RegenerateToken => token = generateToken
  }

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
    pathPrefix("v1" / "rootpath") {
      authenticate(Authenticator()) { user =>
        path("") {
          get {
            complete {
              containers.keys
            }
          }
        } ~
        path(PathElement) { container =>
          put {
            val alreadyExists = containers.putIfAbsent(
              Container(container, 0, 0),
              TrieMap[String, (ObjectMetadata, Object)]()).isDefined
            complete(if (alreadyExists) StatusCodes.Accepted else StatusCodes.Created)
          } ~
          get {
            complete {
              containers.find(_._1.name == container)
                .map(_._2.values)
                .getOrElse(Iterable[(ObjectMetadata, Object)]())
                .map(_._1)
            }
          }
        } ~
        path(PathElement / PathElement) { (container, name) =>
          put { ctx =>
            val contentLength = ctx.request.headers.find(_.is("content-length")).map(_.asInstanceOf[`Content-Length`])
            val contentType = ctx.request.headers.find(_.is("content-type")).map(_.asInstanceOf[`Content-Type`])
            if (contentLength.isEmpty || contentType.isEmpty) ctx.complete(StatusCodes.LengthRequired)
            val entity = ctx.request.entity
            val obj = Object(name, contentType.get.contentType.mediaType, entity.buffer)
            val meta = ObjectMetadata(name,
                                      new String(MessageDigest.getInstance("MD5").digest(entity.buffer)),
                                      entity.buffer.size,
                                      contentType.get.contentType.mediaType.toString,
                                      new Instant)
            val containerEntry = containers.find(_._1.name == container)
            if (containerEntry.isEmpty) ctx.complete(StatusCodes.NotFound)
            containerEntry.get._2.put(name, (meta, obj))
            ctx.complete(StatusCodes.Created)
          } ~
          get { ctx =>
            val containerEntry = containers.find(_._1.name == container)
            if (containerEntry.isEmpty) {
              ctx.complete(StatusCodes.NotFound)
            } else {
              val objectEntry = containerEntry.get._2.get(name)
              if (objectEntry.isEmpty) {
                ctx.complete(StatusCodes.NotFound)
              } else {
                val response = HttpResponse(StatusCodes.OK,
                  HttpBody(ContentType(objectEntry.get._2.mediaType), objectEntry.get._2.data))
                ctx.complete(response)
              }
            }
          } ~
          delete { ctx =>
            val containerEntry = containers.find(_._1.name == container)
            if (containerEntry.isEmpty) {
              ctx.complete(StatusCodes.NotFound)
            } else {
              val objectEntry = containerEntry.get._2.get(name)
              if (objectEntry.isEmpty) {
                ctx.complete(StatusCodes.NotFound)
              } else {
                containerEntry.get._2.remove(name)
                ctx.complete(StatusCodes.OK)
              }
            }
          }
        }
      }
    }

  class Authenticator extends HttpAuthenticator[String] {
    def scheme = "Swift"
    def realm = "Swift"
    def params(ctx: RequestContext) = Map.empty

    implicit def executionContext = ec

    def authenticate(credentials: Option[HttpCredentials], ctx: RequestContext) = {
      val userTokenO = ctx.request.headers.find(_.is("x-storage-token"))
        .map(_.value)
        .filter(_ == token)
      Future.successful(userTokenO)
    }
  }

  object Authenticator {
    def apply() = new Authenticator
  }
}
