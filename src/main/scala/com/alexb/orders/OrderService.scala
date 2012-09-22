package com.alexb.orders

import cc.spray.Directives
import cc.spray.directives.PathElement
import cc.spray.http.StatusCodes
import cc.spray.typeconversion.SprayJsonSupport
import akka.util.Timeout
import akka.util.duration._
import akka.actor.ActorRef
import akka.pattern.ask

trait OrderService
	extends Directives
	with SprayJsonSupport
	with OrderMarshallers {
	
	implicit val timeout = Timeout(5 seconds) // needed for `?` below

	implicit def orderActor: ActorRef

	val orderService = {
		pathPrefix("orders") {
			path("save-order") {
				post {
					content(as[AddOrderCommand]) { cmd =>
						orderActor ! cmd
						respondWithStatus(StatusCodes.Accepted) {
							completeWith { 
								"" 
							}
						}
					}
				}
			} ~
			path("get-orders" / PathElement) { clientId =>
				get {
					completeWith((orderActor ? OrdersByClientIdQuery(clientId)).mapTo[List[Order]])
				}
			} ~
			path("test") {
				completeWith(Order("1", "1", List(OrderItem("Trololo", 2))))
			}
		} 
	}
}
