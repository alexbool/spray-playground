package com.alexb.user

import org.scalatest._
import org.scalamock.scalatest.MockFactory
import spray.testkit.ScalatestRouteTest
import spray.httpx.SprayJsonSupport
import spray.http.StatusCodes

class UserServiceSpec extends WordSpec with MustMatchers with ScalatestRouteTest with MockFactory
  with SprayJsonSupport with UserMarshallers {

  "UserService" must {
    "check username availability" in {
      val stubRepository = stub[UserRepository]
      val service = new UserService(stubRepository)(system.dispatcher)
      (stubRepository.checkUsernameFree _).when("existent-user").returns(false)
      (stubRepository.checkUsernameFree _).when("non-existent-user").returns(true)
      Get("/users/check-username-free?username=existent-user") ~> service.route ~> check {
        entityAs[CheckResult] must be(CheckResult(false))
      }
      Get("/users/check-username-free?username=non-existent-user") ~> service.route ~> check {
        entityAs[CheckResult] must be(CheckResult(true))
      }
    }
    "check credentials" in {
      val stubRepository = stub[UserRepository]
      val service = new UserService(stubRepository)(system.dispatcher)
      (stubRepository.checkCredentials _).when("alex", "passw0rd").returns(true)
      (stubRepository.checkCredentials _).when(*, *).returns(false)
      Get("/users/check-credentials?username=alex&password=passw0rd") ~> service.route ~> check {
        entityAs[CheckResult] must be(CheckResult(true))
      }
      Get("/users/check-credentials?username=mike&password=foo") ~> service.route ~> check {
        entityAs[CheckResult] must be(CheckResult(false))
      }
    }
    "register users" in {
      val stubRepository = stub[UserRepository]
      val service = new UserService(stubRepository)(system.dispatcher)
      Post("/users/register", RegisterUserCommand("alex", "password")) ~> service.route ~> check {
        response.status == StatusCodes.Created
        (stubRepository.save _).verify(where { u: User => u.username == "alex" && u.password == "password" })
      }
    }
  }
}
