package com.alexb.oauth

import scala.concurrent.ExecutionContext
import spray.http.{HttpChallenge, HttpRequest, OAuth2BearerToken, HttpCredentials}
import spray.routing.RequestContext
import spray.routing.authentication.HttpAuthenticator
import spray.http.HttpHeaders.`WWW-Authenticate`

class OAuthAuthenticator[U](tokenValidator: OAuthTokenValidator[U])(implicit ctx: ExecutionContext)
  extends HttpAuthenticator[U] {

  implicit def executionContext = ctx

  def scheme = "Bearer"
  def params(ctx: RequestContext) = Map.empty
  def realm = "OAuth"

  def authenticate(credentials: Option[HttpCredentials], ctx: RequestContext) = {
    tokenValidator {
      credentials.flatMap {
        case OAuth2BearerToken(token) => Some(token)
        case _ => None
      }
    }
  }

  def getChallengeHeaders(httpRequest: HttpRequest) =
    `WWW-Authenticate`(HttpChallenge(scheme = scheme, realm = realm, params = Map.empty)) :: Nil
}

object OAuth {
  def apply[U](tokenValidator: OAuthTokenValidator[U])(implicit ctx: ExecutionContext) =
    new OAuthAuthenticator(tokenValidator)
}
