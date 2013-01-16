package com.alexb.oauth

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.actor.Props
import akka.pattern.ask
import scala.util.Random
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.can.server.SprayCanHttpServerApp
import spray.client.HttpClient
import akka.util.Timeout
import language.postfixOps

class OAuthdTokenValidatorSpec extends WordSpec with MustMatchers with SprayCanHttpServerApp {

  val timeout = 20 seconds
  implicit val askTimeout = Timeout(timeout)
  val httpClient = system.actorOf(Props(new HttpClient))

  val port = Stream.continually(Random.nextInt(65536)).find(_ > 49152).get
  val stubOAuthdServer = system.actorOf(Props(new StubOAuthdServer))
  val server = newHttpServer(stubOAuthdServer)
  Await.ready(server ? Bind("localhost", port), timeout)

  val tokenValidator = new OAuthdTokenValidator(httpClient, s"http://localhost:$port/user")(system.dispatcher)

  "oauthd token validator" must {
    "validate tokens" in {
      Await.result(tokenValidator(Some("olololo")), timeout) must be (Some(StubOAuthdServer.stubUser))
    }
  }
}
