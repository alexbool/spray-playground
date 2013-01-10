package com.alexb.oauth

import akka.actor.Actor
import spray.routing.{RequestContext, HttpService}
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.authentication.HttpAuthenticator
import spray.http.{OAuth2BearerToken, HttpCredentials}
import scala.concurrent.{Future, ExecutionContext}

class StubOAuthdServer extends Actor with HttpService with SprayJsonSupport with DefaultJsonProtocol {

  def actorRefFactory = context.system
  def receive = runRoute(oauthRoute)

  implicit val userFormat = jsonFormat3(User)
  private val authenticator = new StubAuthenticator

  val oauthRoute =
    path("user") {
      authenticate(authenticator) { user =>
        get {
          complete(StubOAuthdServer.stubUser)
        }
      }
    }

  private class StubAuthenticator(implicit ec: ExecutionContext) extends HttpAuthenticator[String] {
    implicit def executionContext = ec
    def scheme = "Bearer"
    def realm = "OAuth"
    def params(ctx: RequestContext) = Map.empty
    def authenticate(credentials: Option[HttpCredentials], ctx: RequestContext): Future[Option[String]] = {
      Future.successful(credentials.flatMap {
        case OAuth2BearerToken(token) => Some(token)
        case _ => None
      })
    }
  }
}

object StubOAuthdServer {
  val stubUser = User("some_uid", Some("login"), Seq("role_user"))
}
