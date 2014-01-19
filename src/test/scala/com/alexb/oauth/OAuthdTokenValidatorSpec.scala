package com.alexb.oauth

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import akka.io.IO
import org.scalatest.{Matchers, WordSpec}
import scala.util.Random
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.can.Http

import language.postfixOps

class OAuthdTokenValidatorSpec extends WordSpec with Matchers {

  implicit val system = ActorSystem()
  val timeout = 20 seconds
  implicit val askTimeout: Timeout = timeout

  val port = Stream.continually(Random.nextInt(65536)).find(_ > 49152).get
  val stubOAuthdServer = system.actorOf(Props(new StubOAuthdServer))
  Await.ready(IO(Http) ? Http.Bind(stubOAuthdServer,"localhost", port), timeout)

  val tokenValidator = new OAuthdTokenValidator(s"http://localhost:$port/user")(system, system.dispatcher, askTimeout)

  "oauthd token validator" should {
    "validate tokens" in {
      Await.result(tokenValidator(Some("olololo")), timeout) should be (Some(StubOAuthdServer.stubUser))
    }
  }
}
