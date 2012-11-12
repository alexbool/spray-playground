package com.alexb.calculator

import akka.actor.{ Actor, ActorLogging }

class AddCommandListener extends Actor with ActorLogging {

  def receive = {
    case cmd: AddCommand =>
      log.info("AddCommandListener noticed AddCommand(%.2f, %.2f)".format(cmd.a, cmd.b))
  }
}