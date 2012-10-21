package com.alexb.memoize

import org.scalatest._
import Memoize._

class MemoizeSpec extends WordSpec with MustMatchers {

	implicit val cacheManager = new ConcurrentHashMapCacheManager
	
	"Memoize function" must {
		"cache other functions" in {
			def memoizedInc = memoize { a: Int => a + 1}
			memoizedInc(1) must equal(2)
			memoizedInc(1) must equal(2)
		}
	}
}
