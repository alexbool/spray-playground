package com.alexb.user

import org.scalatest._
import com.alexb.test.Config
import com.alexb.main.context.{Context, MongoFromAppContext}
import org.joda.time.Instant

class MongoUserRepositorySpec extends WordSpec with MustMatchers with Config
  with MongoFromAppContext with MongoUserRepository
  with BeforeAndAfterEach with BeforeAndAfterAll {

  override protected def beforeAll() {
    Context.initialize()
  }

  override def beforeEach() {
    clear()
    save(user)
  }

  val user = User("alexb", "changeme", Instant.now)

  "MongoUserRepository" ignore {
    "save and find users" in {
      find("alexb") must equal (Some(user))
    }
    "check user credentials" in {
      checkCredentials(user.username, user.password) must be (true)
      checkCredentials(user.username, "wrong password") must be (false)
    }
    "check for free usernames" in {
      checkUsernameFree(user.username) must be (false)
      checkUsernameFree("a_free_username") must be (true)
    }
    "not insert duplicate users" in {
      evaluating { save(user) } must produce [DuplicateUsernameException]
    }
  }
}
