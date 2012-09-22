package com.alexb.main

import akka.actor.ActorSystem
import com.mongodb.casbah.Imports._
import com.typesafe.config.Config

trait MongoContext {

	implicit def actorSystem: ActorSystem
	
	// MongoDB connection instance
	implicit def mongoConn = MongoConnection(actorSystem.settings.config.getString("mongo.host"))
}
