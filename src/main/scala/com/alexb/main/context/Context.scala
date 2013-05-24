package com.alexb.main.context

import akka.actor.ActorSystem
import com.mongodb.casbah.MongoConnection
import com.alexb.infinispan.InfinispanCacheManager
import org.infinispan.manager.DefaultCacheManager
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import com.alexb.calculator.CalculatorServiceContext
import com.alexb.orders.OrderServiceContext

object Context extends CalculatorServiceContext with OrderServiceContext
  with OAuthdSupport with ActorSystemContext with ActorSystemConfiguration with MongoSupport with ElasticSearchSupport
  with Initializable {
  val actorSystem = ActorSystem("spray-playground")
  val mongoDb = MongoConnection(config.getString("mongo.host"))(config.getString("mongo.db"))
  val cacheManager = new InfinispanCacheManager(new DefaultCacheManager(config.getString("infinispan.config")))
  val elasticSearchClient =
    new TransportClient().addTransportAddress(new InetSocketTransportAddress(config.getString("elasticsearch.host"), 9300))
}

// Some DI traits
trait ActorSystemFromAppContext extends ActorSystemContext {
  def actorSystem = Context.actorSystem
}

trait MongoFromAppContext extends MongoSupport {
  def mongoDb = Context.mongoDb
}

trait InfinispanFromAppContext extends Caching {
  def cacheManager = Context.cacheManager
}

trait ElasticSearchFromAppContext extends ElasticSearchSupport {
  def elasticSearchClient = Context.elasticSearchClient
}
