package com.alexb.main
package context

import akka.actor.ActorSystem
import com.typesafe.config.Config
import spray.io.IOBridge
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
  def ioBridge: IOBridge
}
