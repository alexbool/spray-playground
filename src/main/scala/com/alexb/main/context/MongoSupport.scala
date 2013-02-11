package com.alexb.main
package context

import com.mongodb.casbah.MongoDB

trait MongoSupport {
  // MongoDB database instance
  def mongoDb: MongoDB
}
