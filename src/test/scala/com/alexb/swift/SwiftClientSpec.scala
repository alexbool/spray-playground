package com.alexb.swift

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._
import scala.util.Random
import spray.can.Http
import spray.http.MediaTypes
import scala.io.Source
import language.postfixOps

class SwiftClientSpec extends WordSpec with Matchers {
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = system.dispatcher
  val timeout = 3 seconds
  implicit val askTimeout: Timeout = timeout

  val port = Stream.continually(Random.nextInt(65536)).find(_ > 49152).get
  val mockSwiftServer = system.actorOf(Props(new StubSwiftServer), "stub-swift-server")
  Await.ready(IO(Http) ? Http.Bind(mockSwiftServer, "localhost", port), timeout)

  val client = system.actorOf(
    Props(new SwiftClient(Credentials("some_account", "some_auth_key"), s"http://localhost:$port/v1.0")),
    "swift-client")

  val bytes = Source.fromFile("src/test/resources/sample.png")(scala.io.Codec.ISO8859).map(_.toByte).toArray

  "Swift client" should {
    "fail when Swift authentication fails" in {
      mockSwiftServer ! FailOnNextRequest
      val req = client ? ListContainers
      Await.ready(req, timeout) // If this times out, TimeoutException will be thrown. This exception is not the correct behaviour
      intercept[SwiftException] {
        req.value.get.get
      }
    }
    "create containers" in {
      Await.result(client ? CreateContainer("new_container"), timeout) should be (CreateContainerResult(true, false))
    }
    "create duplicate containers" in {
      Await.result(client ? CreateContainer("new_container"), timeout) should be (CreateContainerResult(true, true))
    }
    "list containers" in {
      Await.result(client ? ListContainers, timeout) should be (Seq(Container("new_container", 0, 0)))
    }
    "list objects" in {
      Await.result(client ? ListObjects("new_container"), timeout) should be (Seq())
    }
    "put objects" in {
      Await.result(client ? PutObject("new_container", "sample.png", bytes), timeout) should be (PutObjectResult(true))
    }
    "get objects" in {
      val obj = Await.result((client ? GetObject("new_container", "sample.png")).mapTo[Option[Object]], timeout).get
      obj.name should be ("sample.png")
      obj.mediaType should be (MediaTypes.forExtension("png").get)
      obj.data.deep should be (bytes.deep)
    }
    "get empty objects" in {
      Await.result(client ? GetObject("new_container", "some_nonexistent_object"), timeout) should be (None)
    }
    "delete existent objects" in {
      Await.result(client ? DeleteObject("new_container", "sample.png"), timeout) should be (DeleteObjectResult(true, false))
    }
    "delete nonexistent objects" in {
      Await.result(client ? DeleteObject("new_container", "sample.png"), timeout) should be (DeleteObjectResult(true, true))
    }
    "handle interleaving requests" in {
      val req1 = client ? ListContainers
      val req2 = client ? ListContainers
      val f = for {
        res1 <- req1.mapTo[Seq[Container]]
        res2 <- req2.mapTo[Seq[Container]]
      } yield (res1, res2)
      val readyResult = Await.result(f, timeout)
      readyResult._1.length should be (1)
      readyResult._2.length should be (1)
    }
    "handle authentication token expiration" in {
      mockSwiftServer ! RegenerateToken
      Await.result(client ? ListContainers, timeout)
    }
    "fail when Swift server fails" in {
      mockSwiftServer ! FailOnNextRequest
      val req = client ? ListContainers
      Await.ready(req, timeout) // If this times out, TimeoutException will be thrown. This exception is not the correct behaviour
      intercept[SwiftException] {
        req.value.get.get
      }
    }
  }
}
