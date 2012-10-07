package com.alexb.orders

import cc.spray.routing.directives.PathMatchers._
import cc.spray.routing.HttpService
import cc.spray.http.StatusCodes
import cc.spray.httpx.SprayJsonSupport
import akka.util.Timeout
import akka.actor.ActorRef
import akka.pattern.ask

trait OrderService
	extends HttpService
	with SprayJsonSupport
	with OrderMarshallers {
	
	implicit val timeout: Timeout // needed for `?` below

	def orderActor: ActorRef
	def orderSearchActor: ActorRef

	val orderRoute = {
		pathPrefix("orders") {
			path("save-order") {
				post {
					entity(as[AddOrderCommand]) { cmd =>
						orderActor ! cmd
						respondWithStatus(StatusCodes.Accepted) {
							complete("")
						}
					}
				}
			} ~
			path("orders" / PathElement) { orderId =>
				get {
					complete((orderActor ? OrderByIdQuery(orderId)).mapTo[Option[Order]])
				}
			} ~
			path("orders-by-client" / PathElement) { clientId =>
				get {
					complete((orderActor ? OrdersByClientIdQuery(clientId)).mapTo[List[Order]])
				}
			} ~
			path("search-by-notes") {
				get {
					parameter('query.as[String]) { query =>
						complete((orderSearchActor ? SearchOrdersByNotesQuery(query)).mapTo[Seq[Order]])
					}
				}
			} ~
			path("test") {
				complete(Order("1", "1", List(OrderItem("Trololo", 2)), "Some notes"))
			}
		} 
	}
}
