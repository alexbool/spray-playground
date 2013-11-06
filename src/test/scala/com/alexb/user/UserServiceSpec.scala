package com.alexb.user

import org.scalatest._
import spray.testkit.ScalatestRouteTest
import spray.httpx.SprayJsonSupport
import spray.http.StatusCodes
import language.reflectiveCalls

class UserServiceSpec extends WordSpec with Matchers with ScalatestRouteTest
  with SprayJsonSupport with UserMarshallers {

  "UserService" should {
    "check username availability" in {
      val stubRepository = new UserRepository {
        def clear() = ???
        def checkCredentials(username: String, password: String) = ???
        def save(user: User) = ???
        def find(username: String) = ???
        def checkUsernameFree(username: String) = username match {
          case "existent-user" => false
          case _               => true
        }
      }
      val service = new UserService(stubRepository)(system.dispatcher)
      Get("/users/check-username-free?username=existent-user") ~> service.route ~> check {
        responseAs[CheckResult] should be(CheckResult(false))
      }
      Get("/users/check-username-free?username=non-existent-user") ~> service.route ~> check {
        responseAs[CheckResult] should be(CheckResult(true))
      }
    }
    "check credentials" in {
      val stubRepository = new UserRepository {
        def clear() = ???
        def checkCredentials(username: String, password: String) = (username, password) match {
          case ("alex", "passw0rd") => true
          case _                    => false
        }
        def save(user: User) = ???
        def find(username: String) = ???
        def checkUsernameFree(username: String) = ???
      }
      val service = new UserService(stubRepository)(system.dispatcher)
      Get("/users/check-credentials?username=alex&password=passw0rd") ~> service.route ~> check {
        responseAs[CheckResult] should be(CheckResult(true))
      }
      Get("/users/check-credentials?username=mike&password=foo") ~> service.route ~> check {
        responseAs[CheckResult] should be(CheckResult(false))
      }
    }
    "register users" in {
      val stubRepository = new UserRepository {
        var savedUser: Option[User] = None

        def clear() = ???
        def checkCredentials(username: String, password: String) = ???
        def save(user: User) {
          savedUser = Some(user)
        }
        def find(username: String) = ???
        def checkUsernameFree(username: String) = ???
      }
      val service = new UserService(stubRepository)(system.dispatcher)
      Post("/users/register", RegisterUserCommand("alex", "password")) ~> service.route ~> check {
        response.status == StatusCodes.Created
        stubRepository.savedUser.get.username should be("alex")
        stubRepository.savedUser.get.password should be("password")
      }
    }
  }
}
