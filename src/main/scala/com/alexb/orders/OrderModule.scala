package com.alexb.orders

import akka.actor.Props
import com.alexb.main.context._

trait OrderModule extends OrderService {
  this: MongoSupport with ElasticSearchSupport with OAuthSupport with ActorSystemContext =>

  private lazy val orderActorRef = actorSystem.actorOf(
    props = Props(new OrderActor(mongoDb("orders"))))

  def orderActor = orderActorRef

  private lazy val orderSearchActorRef = actorSystem.actorOf(
    props = Props(new OrderSearchActor(elasticSearchClient, "spray_playground")))

  def orderSearchActor = orderSearchActorRef
}
