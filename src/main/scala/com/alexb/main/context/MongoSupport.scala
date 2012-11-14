package com.alexb.main
package context

import com.mongodb.casbah.Imports._

trait MongoSupport {
  this: Configuration =>

  // MongoDB database instance
  val mongoDb = MongoConnection(config.getString("mongo.host"))(config.getString("mongo.db"))
}
