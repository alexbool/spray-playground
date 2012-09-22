package com.alexb.orders

import akka.actor.Actor
import com.mongodb.casbah.Imports._
import java.util.UUID

class OrderActor(collection: MongoCollection) extends Actor {

	def receive = {
		case cmd: AddOrderCommand => saveOrder(Order(UUID.randomUUID.toString, cmd.clientId, List()))
		case cmd: DeleteOrderCommand => deleteOrder(cmd.orderId)
		case cmd: OrderByIdQuery => sender ! findOrder(cmd.orderId)
		case cmd: OrdersByClientIdQuery => sender ! findOrdersByClient(cmd.clientId)
	}
	
	private def saveOrder(order: Order) = {
		collection += MongoDBObject("_id" -> order.orderId,
									"clientId" -> order.clientId,
									"items" -> order.items)
	}
	
	private def deleteOrder(orderId: String) = {
		collection -= MongoDBObject("_id" -> orderId)
	}
	
	private def findOrder(orderId: String) = 
		collection
			.findOne(MongoDBObject("_id" -> orderId))
			.map(toOrder)
	
	private def findOrdersByClient(clientId: String) = 
		collection
			.find(MongoDBObject("clientId" -> clientId))
			.map(toOrder)
			.toList
	
	private val toOrder: DBObject => Order =
		d => Order(d("_id").asInstanceOf[String],
					d("clientId").asInstanceOf[String],
					d("items").asInstanceOf[List[DBObject]].map(toOrderItem))
	
	private val toOrderItem: DBObject => OrderItem =
		d => OrderItem(d("itemId").asInstanceOf[String], d("quantity").asInstanceOf[Int])
}
