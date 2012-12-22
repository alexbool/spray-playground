package com.alexb.user

import com.mongodb.casbah.Imports._
import org.scala_tools.time.Imports._
import org.joda.time.Instant
import com.mongodb.MongoException
import com.alexb.main.context.MongoSupport

class DuplicateUsernameException extends IllegalArgumentException("Cannot create user with duplicate username")

trait UserRepository {
  def find(username: String): Option[User]
  def checkUsernameFree(username: String): Boolean
  def checkCredentials(username: String, password: String): Boolean
  def save(user: User)
  def clear()
}

trait MongoUserRepository extends UserRepository {
  this: MongoSupport =>

  implicit private val writeConcern = WriteConcern.Safe

  private val userCollection = mongoDb("users")

  def find(username: String) =
    userCollection
      .findOne(MongoDBObject("_id" -> username))
      .map(d => User(d.getAs[String]("_id").get,
        d.getAs[String]("password").get,
        new Instant(d.getAs[String]("created").get)))

  def checkCredentials(username: String, password: String) =
    userCollection.count((MongoDBObject("_id" -> username, "password" -> password))) == 1L

  def checkUsernameFree(username: String) =
    userCollection.count((MongoDBObject("_id" -> username))) == 0L

  def save(user: User) {
    try {
      userCollection insert MongoDBObject("_id" -> user.username,
        "password" -> user.password,
        "created" -> user.created.toDate)
    }
    catch {
      case e: MongoException.DuplicateKey => throw new DuplicateUsernameException
    }
  }

  def clear() {
    userCollection remove MongoDBObject()
  }
}
