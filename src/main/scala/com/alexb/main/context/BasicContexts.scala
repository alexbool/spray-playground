package com.alexb.main
package context

import akka.actor.ActorSystem
import com.typesafe.config.Config
import spray.io.IOBridge

trait ActorSystemContext {
  def actorSystem: ActorSystem
}

trait Configuration {
  def config: Config
}

trait IOBridgeContext {
  def ioBridge: IOBridge
}
