package com.alexb.oauth

import akka.actor.ActorRefFactory
import akka.util.Timeout
import spray.http.{OAuth2BearerToken, HttpRequest}
import spray.http.HttpHeaders.Authorization
import spray.httpx.UnsuccessfulResponseException
import spray.httpx.SprayJsonSupport._
import spray.client.pipelining._
import spray.json.DefaultJsonProtocol._
import scala.concurrent.{ Future, ExecutionContext }

class OAuthdTokenValidator(url: String)(implicit refFactory: ActorRefFactory, executionContext: ExecutionContext,
                                        futureTimeout: Timeout)
  extends OAuthTokenValidator[User] {

  implicit val userFormat = jsonFormat3(User)

  val pipeline: Token => HttpRequest => Future[User] = { token =>
    addHeader(Authorization(OAuth2BearerToken(token))) ~>
    sendReceive ~>
    unmarshal[User]
  }

  def apply(token: Option[Token]) = token match {
    case Some(tkn) => pipeline(tkn)(Get(url)).map(Some(_)).recover({ case _: UnsuccessfulResponseException => None })
    case None      => Future.successful(None)
  }
}
