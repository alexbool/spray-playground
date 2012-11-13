package com.alexb.test

import com.typesafe.config.ConfigFactory
import com.alexb.main.context.Configuration

trait Config extends Configuration {
	val config = ConfigFactory.load("application.conf")
}

object Config extends Config
