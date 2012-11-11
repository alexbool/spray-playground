package com.alexb.main
package context

import com.mongodb.casbah.Imports._

trait MongoContext {
	this: ConfigContext =>
	
	// MongoDB connection instance
	def mongoConn = MongoConnection(config.getString("mongo.host"))
}
