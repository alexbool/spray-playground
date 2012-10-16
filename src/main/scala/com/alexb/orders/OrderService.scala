package com.alexb.orders

import spray.routing.directives.PathMatchers._
import spray.routing.HttpService
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import akka.util.Timeout
import akka.actor.ActorRef
import akka.pattern.ask
import com.alexb.utils.PageDirectives

trait OrderService
	extends HttpService
	with PageDirectives
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
			get {
				path("orders" / PathElement) { orderId =>
					complete((orderActor ? OrderByIdQuery(orderId)).mapTo[Option[Order]])
				} ~
				path("orders-by-client" / PathElement) { clientId =>
					pageInfo { page =>
						complete((orderActor ? OrdersByClientIdQuery(clientId, page)).mapTo[List[Order]])
					}
				} ~
				path("search-by-notes") {
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
