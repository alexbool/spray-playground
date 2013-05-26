package com.alexb.main
package context

import com.mongodb.casbah.{MongoConnection, MongoDB}

trait MongoSupport {
  // MongoDB database instance
  def mongoDb: MongoDB
}

trait DefaultMongo extends MongoSupport { this: Configuration =>
  lazy val mongoDb = MongoConnection(config.getString("mongo.host"))(config.getString("mongo.db"))
}
