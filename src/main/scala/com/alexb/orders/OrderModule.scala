package com.alexb.orders

import akka.actor.{ ActorSystem, Props }
import com.mongodb.casbah.MongoCollection
import org.elasticsearch.client.Client
import com.alexb.oauth.{ OAuthTokenValidator, User }

trait OrderModule extends OrderService {

	def actorSystem: ActorSystem
	def collection: MongoCollection
	def elasticSearchClient: Client
	def tokenValidator: OAuthTokenValidator[User]

	private lazy val orderActorRef = actorSystem.actorOf(
		props = Props(new OrderActor(collection)))

	def orderActor = orderActorRef

	private lazy val orderSearchActorRef = actorSystem.actorOf(
		props = Props(new OrderSearchActor(elasticSearchClient, "spray_playground")))

	def orderSearchActor = orderSearchActorRef
}
