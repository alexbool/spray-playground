package com.alexb.main
package context

import akka.actor.Props
import com.alexb.oauth._
import spray.client.HttpClient

trait OAuthSupport {
  def tokenValidator: OAuthTokenValidator[User]
}

trait OAuthdSupport extends OAuthSupport {
  this: ActorSystemContext with Configuration with IOBridgeContext =>

  private val httpClient = actorSystem.actorOf(
    props = Props(new HttpClient),
    name = "oauth-http-client")

  val tokenValidator = new OAuthdTokenValidator(httpClient,
    url(config.getString("oauth.host"), config.getInt("oauth.port")))(actorSystem.dispatcher)

  private def url(host: String, port: Int) = s"http://$host:$port/user"
}
