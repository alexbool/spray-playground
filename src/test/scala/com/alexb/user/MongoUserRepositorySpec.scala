package com.alexb.user

import org.scalatest._
import com.alexb.main.context.DefaultMongo
import com.alexb.test.Config
import org.joda.time.Instant
import language.reflectiveCalls

class MongoUserRepositorySpec extends WordSpec with MustMatchers
  with BeforeAndAfterEach with BeforeAndAfterAll {

  val context = new DefaultMongo with Config {
    val userRepository = new MongoUserRepository(mongoDb)
  }

  val repository = context.userRepository

  override def beforeEach() {
    repository.clear()
    repository.save(user)
  }

  val user = User("alexb", "changeme", Instant.now)

  "MongoUserRepository" ignore {
    "save and find users" in {
      repository.find("alexb") must equal (Some(user))
    }
    "check user credentials" in {
      repository.checkCredentials(user.username, user.password) must be (true)
      repository.checkCredentials(user.username, "wrong password") must be (false)
    }
    "check for free usernames" in {
      repository.checkUsernameFree(user.username) must be (false)
      repository.checkUsernameFree("a_free_username") must be (true)
    }
    "not insert duplicate users" in {
      evaluating { repository.save(user) } must produce [DuplicateUsernameException]
    }
  }
}
