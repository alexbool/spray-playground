package com.alexb.calculator

import akka.actor.{ Actor, ActorLogging }

class AddCommandListener extends Actor with ActorLogging {

  def receive = {
    case cmd: AddCommand =>
      log.info(f"AddCommandListener noticed AddCommand(${cmd.a}%.2f, ${cmd.b}%.2f)")
  }
}