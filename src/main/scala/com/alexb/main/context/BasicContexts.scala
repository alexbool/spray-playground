package com.alexb.main
package context

import akka.actor.ActorSystem
import com.typesafe.config.Config
import com.alexb.memoize.CacheManager

trait ActorSystemContext {
  def actorSystem: ActorSystem
}

trait Configuration {
  def config: Config
}

trait ActorSystemConfiguration extends Configuration {
  this: ActorSystemContext =>
  def config = actorSystem.settings.config
}

trait Caching {
  def cacheManager: CacheManager
}
