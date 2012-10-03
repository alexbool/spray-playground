package com.alexb.orders

import akka.actor.{ ActorSystem, Props }
import com.mongodb.casbah.MongoCollection

trait OrderModule extends OrderService {

	implicit def actorSystem: ActorSystem
	implicit def collection: MongoCollection
	
	private lazy val orderActorRef = actorSystem.actorOf(
		props = Props(new OrderActor(collection)))
		
	def orderActor = orderActorRef
}
