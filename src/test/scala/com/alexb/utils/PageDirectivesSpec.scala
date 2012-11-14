package com.alexb.utils

import spray.routing.Directives
import spray.testkit._
import org.scalatest._

class PageDirectivesSpec extends WordSpec with MustMatchers with Directives with PageDirectives with ScalatestRouteTest {
	"Page directive" must {
		"extract URL parameters" in {
			Get("/?page=3&size=20") ~> pageInfo(p => complete(p.num + ":" + p.size)) ~> check {
				entityAs[String] === "3:20"
			}
		}
		"substitute correct default values" in {
			Get("/") ~> pageInfo(p => complete(p.num + ":" + p.size)) ~> check {
				entityAs[String] === "1:10"
			}
		}
	}
}