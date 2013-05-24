package com.alexb.main
package context

import scala.concurrent.duration._
import com.alexb.oauth._

trait OAuthSupport {
  def tokenValidator: OAuthTokenValidator[User]
}

trait OAuthdSupport extends OAuthSupport {
  this: ActorSystemContext with Configuration =>

  lazy val tokenValidator = new OAuthdTokenValidator(
    url(config.getString("oauth.host"),
    config.getInt("oauth.port")))(actorSystem, actorSystem.dispatcher, 20.seconds)

  private def url(host: String, port: Int) = s"http://$host:$port/user"
}
