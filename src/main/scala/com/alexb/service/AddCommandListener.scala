package com.alexb.service

import akka.actor.Actor

class AddCommandListener extends Actor {

	def receive = {
		case cmd: AddCommand =>
			System.out.println("AddCommandListener noticed AddCommand(%.2f, %.2f)".format(cmd.a, cmd.b))
	}
}