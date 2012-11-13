package com.alexb.orders

import akka.actor.{ ActorSystem, Props }
import com.mongodb.casbah.MongoCollection
import org.elasticsearch.client.Client
import com.alexb.main.context._

trait OrderModule extends OrderService {
  this: MongoContext with ElasticSearchContext with OAuthContext =>

  private lazy val orderActorRef = actorSystem.actorOf(
    props = Props(new OrderActor(mongoDb("orders"))))

  def orderActor = orderActorRef

  private lazy val orderSearchActorRef = actorSystem.actorOf(
    props = Props(new OrderSearchActor(elasticSearchClient, "spray_playground")))

  def orderSearchActor = orderSearchActorRef
}
