package com.alexb.memoize

import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import language.postfixOps

trait CacheManagerSpec extends WordSpec with Matchers {
	def name: String
	def cacheManager: CacheManager

	name should {
		"implement get method" in {
			cacheManager.get("", "a key") should equal (None)
		}
    "implement getAsync method" in {
      Await.result(cacheManager.getAsync("", "a key"), 10 seconds) should equal (None)
    }
		"implement put method" in {
			cacheManager.put("", "a key", "a value")
			cacheManager.get("", "a key") should equal (Some("a value"))
		}
	}
}

class ConcurrentHashMapCacheManagerSpec extends CacheManagerSpec {
	def name = "ConcurrentHashMapCacheManager"
		
	val manager = new ConcurrentHashMapCacheManager
	def cacheManager = manager
}
