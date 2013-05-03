package com.alexb.oauth

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import scala.util.Random
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.can.Http
import akka.util.Timeout
import akka.io.IO
import language.postfixOps

class OAuthdTokenValidatorSpec extends WordSpec with MustMatchers {

  implicit val system = ActorSystem()
  val timeout = 20 seconds
  implicit val askTimeout = Timeout(timeout)

  val port = Stream.continually(Random.nextInt(65536)).find(_ > 49152).get
  val stubOAuthdServer = system.actorOf(Props(new StubOAuthdServer))
  Await.ready(IO(Http) ? Http.Bind(stubOAuthdServer,"localhost", port), timeout)

  val tokenValidator = new OAuthdTokenValidator(s"http://localhost:$port/user")(system, system.dispatcher, askTimeout)

  "oauthd token validator" must {
    "validate tokens" in {
      Await.result(tokenValidator(Some("olololo")), timeout) must be (Some(StubOAuthdServer.stubUser))
    }
  }
}
