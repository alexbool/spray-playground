package com.alexb.memoize

import org.scalatest._
import Memoize._

class MemoizeSpec extends WordSpec with MustMatchers {

	implicit val cacheManager = new ConcurrentHashMapCacheManager
	
	"Memoize function" must {
		"cache one-arg functions" in {
			def memoized = memoize { a: Int => a + 1 }
			memoized(1) must equal(2)
			memoized(1) must equal(2)
		}
		"cache two-arg functions" in {
			def memoized = memoize { (a: Int, b: Int) => a + b }
			memoized(1, 2) must equal(3)
			memoized(1, 2) must equal(3)
		}
		"cache three-arg functions" in {
			def memoized = memoize { (a: Int, b: Int, c: Int) => a + b + c }
			memoized(1, 1, 1) must equal(3)
			memoized(1, 1, 1) must equal(3)
		}
		"cache four-arg functions" in {
			def memoized = memoize { (a: Int, b: Int, c: Int, d: Int) => a + b + c + d }
			memoized(1, 1, 1, 1) must equal(4)
			memoized(1, 1, 1, 1) must equal(4)
		}
	}
}
