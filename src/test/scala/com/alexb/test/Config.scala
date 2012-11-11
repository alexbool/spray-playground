package com.alexb.test

import com.typesafe.config.ConfigFactory
import com.alexb.main.context.ConfigContext

trait Config extends ConfigContext {
	val config = ConfigFactory.load("application.conf")
}

object Config extends Config
