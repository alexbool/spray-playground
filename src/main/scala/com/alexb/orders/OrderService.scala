package com.alexb.orders

import scala.concurrent.ExecutionContext
import spray.routing.PathMatchers._
import spray.routing.Directives
import spray.http.{StatusCodes, EmptyEntity}
import spray.httpx.SprayJsonSupport
import akka.util.Timeout
import akka.actor.ActorRef
import akka.pattern.ask
import com.alexb.utils.PageDirectives
import com.alexb.oauth.{OAuthTokenValidator, OAuth}
import com.alexb.main.HttpRouteContainer

class OrderService(orderActor: ActorRef, orderSearchActor: ActorRef, tokenValidator: OAuthTokenValidator[_])
                  (implicit ec: ExecutionContext, timeout: Timeout)
  extends Directives with PageDirectives with SprayJsonSupport with OrderMarshallers with HttpRouteContainer {

  val route = {
    pathPrefix("orders") {
      post {
        path("save-order") {
          entity(as[AddOrderCommand]) { cmd =>
            orderActor ! cmd
            respondWithStatus(StatusCodes.Accepted) {
              complete(EmptyEntity)
            }
          }
        }
      } ~
      get {
        path("orders" / Segment) { orderId =>
          complete((orderActor ? OrderByIdQuery(orderId)).mapTo[Option[Order]])
        } ~
        path("orders-by-client" / Segment) { clientId =>
          pageInfo { page =>
            complete((orderActor ? OrdersByClientIdQuery(clientId, page)).mapTo[List[Order]])
          }
        } ~
        path("search-by-notes") {
          (parameter('query.as[String]) & pageInfo) { (query, page) =>
            complete((orderSearchActor ? SearchOrdersByNotesQuery(query, page)).mapTo[Seq[Order]])
          }
        }
      } ~
      path("test") {
        authenticate(OAuth(tokenValidator)) { user =>
          complete(Order("1", "1", List(OrderItem("Trololo", 2)), "Some notes"))
        }
      }
    }
  }
}
