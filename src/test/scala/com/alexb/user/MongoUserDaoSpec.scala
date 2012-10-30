package com.alexb.user

import org.scalatest._
import com.alexb.test.Config
import com.alexb.main.MongoContext
import com.mongodb.casbah.Imports._
import org.joda.time.Instant

class MongoUserDaoSpec extends WordSpec with MustMatchers with MongoUserDao with Config with MongoContext with BeforeAndAfterEach {
	
	val userCollection = mongoConn("spray_playground")("users")
	
	override def beforeEach {
		userCollection remove MongoDBObject()
		save(user)
	}
	
	val user = User("alexb", "changeme", Instant.now)
	
	"MongoUserDao" must {
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
