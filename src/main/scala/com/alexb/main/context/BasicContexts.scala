package com.alexb.main
package context

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.Config
import com.alexb.memoize.CacheManager
import spray.io.IOExtension

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

trait IOBridgeContext {
  this: ActorSystemContext =>
  def ioBridge: ActorRef = IOExtension(actorSystem).ioBridge()
}
