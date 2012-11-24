package com.alexb.memoize

import org.scalatest._
import org.scalamock.scalatest.MockFactory
import Memoize._

class MemoizeSpec extends WordSpec with MustMatchers with MockFactory {

	implicit val cacheManager = new ConcurrentHashMapCacheManager
	
	"Memoize function" must {
		"cache no-arg functions" in {
      val m = mockFunction[Int]
      m.expects().returns(1).once
			val memoized = memoize(m)
			memoized() must equal(1)
			memoized() must equal(1)
		}
		"cache one-arg functions" in {
      val m = mockFunction[Int, Int]
      m.expects(*).returns(2).once
      val memoized = memoize(m)
			memoized(1) must equal(2)
			memoized(1) must equal(2)
		}
		"cache two-arg functions" in {
      val m = mockFunction[Int, Int, Int]
      m.expects(*, *).returns(3).once
      val memoized = memoize(m)
			memoized(1, 2) must equal(3)
			memoized(1, 2) must equal(3)
		}
		"cache three-arg functions" in {
      val m = mockFunction[Int, Int, Int, Int]
      m.expects(*, *, *).returns(3).once
      val memoized = memoize(m)
			memoized(1, 1, 1) must equal(3)
			memoized(1, 1, 1) must equal(3)
		}
		"cache four-arg functions" in {
      val m = mockFunction[Int, Int, Int, Int, Int]
      m.expects(*, *, *, *).returns(4).once
      val memoized = memoize(m)
			memoized(1, 1, 1, 1) must equal(4)
			memoized(1, 1, 1, 1) must equal(4)
		}
		"cache no-arg functions with cache name and key" in {
      val m = mockFunction[Int]
      m.expects().returns(1).once
			def memoized = memoize("Cache name", "key", m)
			memoized() must equal(1)
			memoized() must equal(1)
		}
	}
}
