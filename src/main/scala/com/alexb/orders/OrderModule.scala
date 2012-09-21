package com.alexb.orders

import akka.actor.{ ActorSystem, Props }
import com.mongodb.casbah.MongoCollection

class OrderModule(system: ActorSystem, collection: MongoCollection) {

	implicit def actorSystem = system

	implicit val orderActor = actorSystem.actorOf(
		props = Props(new OrderActor(collection)),
		name = "order-actor")
}