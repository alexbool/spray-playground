package com.alexb.main

import com.mongodb.casbah.Imports._
import com.typesafe.config.Config

trait MongoContext {

	implicit def config: Config
	
	// MongoDB connection instance
	implicit def mongoConn = MongoConnection(config.getString("mongo.host"))
}
