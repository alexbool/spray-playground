package com.alexb.main
package context

import akka.actor.Props
import spray.can.client.HttpClient
import spray.client.HttpConduit
import com.alexb.oauth._

trait OAuthContext {
  def tokenValidator: OAuthTokenValidator[User]
}

trait OAuthdContext extends OAuthContext {
  this: ActorSystemContext with ConfigContext with IOBridgeContext =>

  val httpClient = actorSystem.actorOf(
    props = Props(new HttpClient(ioBridge)),
    name = "oauth-http-client")

  val conduit = actorSystem.actorOf(
    props = Props(new HttpConduit(httpClient, config.getString("oauth.host"), config.getInt("oauth.port"))),
    name = "oauth-http-conduit"
  )

  val tokenValidatorInstance = new OAuthdTokenValidator(conduit)(actorSystem.dispatcher)
  def tokenValidator = tokenValidatorInstance
}
