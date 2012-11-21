package com.alexb.oauth

import scala.concurrent.ExecutionContext
import spray.http.HttpCredentials
import spray.routing.RequestContext
import spray.routing.authentication.HttpAuthenticator
import spray.http.OtherHttpCredentials

class OAuthAuthenticator[U](tokenValidator: OAuthTokenValidator[U])(implicit ctx: ExecutionContext)
  extends HttpAuthenticator[U] {

  implicit def executionContext = ctx

  def scheme = "Bearer"
  def params(ctx: RequestContext) = Map.empty
  def realm = "OAuth"

  def authenticate(credentials: Option[HttpCredentials], ctx: RequestContext) = {
    tokenValidator {
      credentials.flatMap {
        case OtherHttpCredentials(_, params: Map[String, String]) => params.headOption.map(_._2)
        case _ => None
      }
    }
  }
}

object OAuth {
  def apply[U](tokenValidator: OAuthTokenValidator[U])(implicit ctx: ExecutionContext) =
    new OAuthAuthenticator(tokenValidator)
}
