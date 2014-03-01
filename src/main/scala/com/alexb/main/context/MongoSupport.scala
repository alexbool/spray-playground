package com.alexb.main
package context

import com.mongodb.casbah.{MongoClient, MongoDB}

trait MongoSupport {
  // MongoDB database instance
  def mongoDb: MongoDB
}

trait DefaultMongo extends MongoSupport { this: Configuration =>
  lazy val mongoDb = MongoClient(config.getString("mongo.host"))(config.getString("mongo.db"))
}
