package com.alexb.main.context

import akka.actor.ActorSystem
import com.mongodb.casbah.MongoConnection
import com.alexb.infinispan.InfinispanCacheManager
import org.infinispan.manager.DefaultCacheManager

object Context extends ActorSystemContext with ActorSystemConfiguration with MongoSupport with ElasticSearchSupport
  with Caching {
  lazy val actorSystem = ActorSystem("spray-playground")
  lazy val mongoDb = MongoConnection(config.getString("mongo.host"))(config.getString("mongo.db"))
  lazy val cacheManager = new InfinispanCacheManager(new DefaultCacheManager(config.getString("infinispan.config")))
}

trait ActorSystemFromAppContext extends ActorSystemContext {
  def actorSystem = Context.actorSystem
}

trait MongoFromAppContext extends MongoSupport {
  def mongoDb = Context.mongoDb
}

trait InfinispanFromAppContext extends Caching {
  def cacheManager = Context.cacheManager
}
