package com.alexb.main
package context

import akka.actor.Props
import spray.can.client.HttpClient
import spray.client.HttpConduit
import com.alexb.oauth._

trait OAuthSupport {
  def tokenValidator: OAuthTokenValidator[User]
}

trait OAuthdSupport extends OAuthSupport {
  this: ActorSystemContext with Configuration with IOBridgeContext =>

  private val httpClient = actorSystem.actorOf(
    props = Props(new HttpClient(ioBridge)),
    name = "oauth-http-client")

  private val conduit = actorSystem.actorOf(
    props = Props(new HttpConduit(httpClient, config.getString("oauth.host"), config.getInt("oauth.port"))),
    name = "oauth-http-conduit"
  )

  private val tokenValidatorInstance = new OAuthdTokenValidator(conduit)(actorSystem.dispatcher)
  def tokenValidator = tokenValidatorInstance
}
