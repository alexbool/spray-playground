package com.alexb.orders

import scala.concurrent.duration._
import akka.actor.Props
import com.alexb.main.context._
import language.postfixOps

trait OrderServiceContext {
  this: MongoSupport with ElasticSearchSupport with OAuthSupport with ActorSystemContext =>

  private lazy val orderActor = actorSystem.actorOf(
    props = Props(new OrderActor(mongoDb("orders"))))

  private lazy val orderSearchActor = actorSystem.actorOf(
    props = Props(new OrderSearchActor(elasticSearchClient, "spray_playground")))

  lazy val orderService =
    new OrderService(orderActor, orderSearchActor, tokenValidator)(actorSystem.dispatcher, 5 seconds)
}
