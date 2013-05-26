package com.alexb.main.context

import akka.actor.{Props, ActorSystem}
import com.alexb.infinispan.InfinispanCacheManager
import org.infinispan.manager.DefaultCacheManager
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import com.alexb.calculator.CalculatorServiceContext
import com.alexb.orders.OrderServiceContext
import com.alexb.statics.StaticsServiceContext
import com.alexb.user.UserServiceContext
import com.alexb.main.SprayPlaygroundActor

object Context extends CalculatorServiceContext with OrderServiceContext with StaticsServiceContext with UserServiceContext
  with OAuthdSupport with ActorSystemContext with ActorSystemConfiguration with DefaultMongo with ElasticSearchSupport
  with Caching with Initializable {
  val actorSystem = ActorSystem("spray-playground")
  val cacheManager = new InfinispanCacheManager(new DefaultCacheManager(config.getString("infinispan.config")))
  val elasticSearchClient =
    new TransportClient().addTransportAddress(new InetSocketTransportAddress(config.getString("elasticsearch.host"), 9300))

  val httpActor = actorSystem.actorOf(
    props = Props(new SprayPlaygroundActor(Iterable(calculatorService, orderService, staticsService, userService))),
    name = "main")
}
