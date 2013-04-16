package com.alexb.main
package context

import akka.actor.ActorSystem
import com.typesafe.config.Config
import com.alexb.memoize.CacheManager
import scala.concurrent.ExecutionContext

trait ActorSystemContext {
  def actorSystem: ActorSystem
}

trait Configuration {
  def config: Config
}

trait ActorSystemConfiguration extends Configuration { this: ActorSystemContext =>
  def config = actorSystem.settings.config
}

trait ExecutionContextContext {
  def ec: ExecutionContext
}

trait ExecutionContextFromActorSystemContext extends ExecutionContextContext { this: ActorSystemContext =>
  def ec = actorSystem.dispatcher
}

trait Caching {
  def cacheManager: CacheManager
}
