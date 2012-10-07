package com.alexb.main

import com.mongodb.casbah.Imports._
import com.typesafe.config.Config

trait MongoContext {

	def config: Config
	
	// MongoDB connection instance
	def mongoConn = MongoConnection(config.getString("mongo.host"))
}
