package com.alexb.main
package context

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.Config
import com.alexb.memoize.CacheManager

trait ActorSystemContext {
  def actorSystem: ActorSystem
}

trait Configuration {
  def config: Config
}

trait Caching {
  def cacheManager: CacheManager
}

trait IOBridgeContext {
  def ioBridge: ActorRef
}
