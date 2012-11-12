package com.alexb.oauth

import spray.http.HttpCredentials
import spray.routing.RequestContext
import spray.routing.authentication.HttpAuthenticator
import spray.http.OtherHttpCredentials

class OAuthAuthenticator[U](tokenValidator: OAuthTokenValidator[U]) extends HttpAuthenticator[U] {

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
  def apply[U](tokenValidator: OAuthTokenValidator[U]) =
    new OAuthAuthenticator(tokenValidator)
}
