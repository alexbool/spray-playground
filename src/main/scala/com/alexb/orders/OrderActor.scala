package com.alexb.orders

import akka.actor.Actor
import com.mongodb.casbah.Imports._
import java.util.UUID

class OrderActor(collection: MongoCollection) extends Actor {

	def receive = {
		case cmd: AddOrderCommand => saveOrder(Order(UUID.randomUUID.toString, cmd.clientId, List()))
		case cmd: DeleteOrderCommand => deleteOrder(cmd.orderId)
		case cmd: OrdersByClientIdQuery => sender ! getOrdersByClient(cmd.clientId)
	}
	
	private def saveOrder(order: Order) = {
		collection += MongoDBObject("_id" -> order.orderId,
									"clientId" -> order.clientId,
									"items" -> order.items)
	}
	
	private def deleteOrder(orderId: String) = {
		collection -= MongoDBObject("_id" -> orderId)
	}
	
	private def getOrdersByClient(clientId: String) = 
		collection
			.find(MongoDBObject("clientId" -> clientId))
			.map(d => Order(
					d("_id").asInstanceOf[String],
					d("clientId").asInstanceOf[String],
					d("items").asInstanceOf[List[DBObject]].map(f => OrderItem(
							f("itemId").asInstanceOf[String],
							f("quantity").asInstanceOf[Int]))))
			.toList
}
