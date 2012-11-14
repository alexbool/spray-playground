package com.alexb.user

import org.scalatest._
import com.alexb.test.Config
import com.alexb.main.context.MongoSupport
import com.mongodb.casbah.Imports._
import org.joda.time.Instant

class MongoUserDaoSpec extends WordSpec with MustMatchers with Config with MongoSupport with MongoUserDao with BeforeAndAfterEach {
	
	override def beforeEach {
		userCollection remove MongoDBObject()
		save(user)
	}
	
	val user = User("alexb", "changeme", Instant.now)

	"MongoUserDao" ignore {
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
