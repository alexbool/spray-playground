package com.alexb.test

import com.typesafe.config.ConfigFactory

trait Config {

	val config = ConfigFactory.load("application.conf")
}

object Config extends Config
