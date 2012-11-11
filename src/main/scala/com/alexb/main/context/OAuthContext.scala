package com.alexb.main
package context

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.Config
import spray.can.client.HttpClient
import spray.client.HttpConduit
import spray.io.IOBridge
import com.alexb.oauth.OAuthdTokenValidator

trait OAuthContext {

	def actorSystem: ActorSystem
	def config: Config
	def ioBridge: IOBridge

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
