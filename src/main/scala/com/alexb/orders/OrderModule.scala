package com.alexb.orders

import akka.actor.{ ActorSystem, Props }
import com.mongodb.casbah.MongoCollection

trait OrderModule extends OrderService {

	implicit def actorSystem: ActorSystem
	implicit def collection: MongoCollection
	
	def orderActor = actorSystem.actorOf(
		props = Props(new OrderActor(collection)),
		name = "order-actor")
}
