package com.alexb.memoize

import org.scalatest._
import org.scalamock.scalatest.MockFactory
import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import Memoize._
import language.postfixOps

class MemoizeSpec extends WordSpec with MustMatchers with BeforeAndAfterEach with MockFactory {

	implicit val cacheManager = new ConcurrentHashMapCacheManager
  val specialCacheName = "other cache"
  val timeout = 10 seconds
  val sleep = 10

	"Memoize function" must {
		"cache by-name value" in {
      val m = mockFunction[Int]
      m.expects().returns(1).once()
			val memoized = memoizeValue("a key", m())
			memoized() must equal(1)
			memoized() must equal(1)
		}
    "cache no-arg functions" in {
      val m = mockFunction[Int]
      m.expects().returns(1).once()
      val memoized = memoize(m)
      memoized() must equal(1)
      memoized() must equal(1)
    }
		"cache one-arg functions" in {
      val m = mockFunction[Int, Int]
      m.expects(*).returns(2).once()
      val memoized = memoize(m)
			memoized(1) must equal(2)
			memoized(1) must equal(2)
		}
		"cache two-arg functions" in {
      val m = mockFunction[Int, Int, Int]
      m.expects(*, *).returns(3).once()
      val memoized = memoize(m)
			memoized(1, 2) must equal(3)
			memoized(1, 2) must equal(3)
		}
		"cache three-arg functions" in {
      val m = mockFunction[Int, Int, Int, Int]
      m.expects(*, *, *).returns(3).once()
      val memoized = memoize(m)
			memoized(1, 1, 1) must equal(3)
			memoized(1, 1, 1) must equal(3)
		}
		"cache four-arg functions" in {
      val m = mockFunction[Int, Int, Int, Int, Int]
      m.expects(*, *, *, *).returns(4).once()
      val memoized = memoize(m)
			memoized(1, 1, 1, 1) must equal(4)
			memoized(1, 1, 1, 1) must equal(4)
		}
		"cache no-arg functions with cache name and key" in {
      val m = mockFunction[Int]
      m.expects().returns(1).once()
			def memoized = memoize(specialCacheName, "key", m)
			memoized() must equal(1)
			memoized() must equal(1)
		}
	}

  "Async memoize function" must {
    "cache by-name values" in {
      val f = stubFunction[Int]
      f.when().returns(1)
      val memoized = memoizeAsyncValue("a key", Future(f()))
      Await.result(memoized(), timeout) must equal(1)
      Thread.sleep(sleep)
      Await.result(memoized(), timeout) must equal(1)
      f.verify().once()
    }
    "cache no-arg functions" in {
      val f = stubFunction[Int]
      f.when().returns(1)
      val memoized = memoizeAsync(() => Future(f()))
      Await.result(memoized(), timeout) must equal(1)
      Thread.sleep(sleep)
      Await.result(memoized(), timeout) must equal(1)
      f.verify().once()
    }
    "cache one-arg functions" in {
      val f = stubFunction[Int, Int]
      f.when(*).returns(2)
      val memoized = memoizeAsync((a: Int) => Future(f(a)))
      Await.result(memoized(1), timeout) must equal(2)
      Thread.sleep(sleep)
      Await.result(memoized(1), timeout) must equal(2)
      f.verify(*).once()
    }
    "cache two-arg functions" in {
      val f = stubFunction[Int, Int, Int]
      f.when(*, *).returns(3)
      val memoized = memoizeAsync((a: Int, b: Int) => Future(f(a, b)))
      Await.result(memoized(1, 2), timeout) must equal(3)
      Thread.sleep(sleep)
      Await.result(memoized(1, 2), timeout) must equal(3)
      f.verify(*, *).once()
    }
    "cache three-arg functions" in {
      val f = stubFunction[Int, Int, Int, Int]
      f.when(*, *, *).returns(3)
      val memoized = memoizeAsync((a: Int, b: Int, c: Int) => Future(f(a, b, c)))
      Await.result(memoized(1, 1, 1), timeout) must equal(3)
      Thread.sleep(sleep)
      Await.result(memoized(1, 1, 1), timeout) must equal(3)
      f.verify(*, *, *).once()
    }
    "cache four-arg functions" in {
      val f = stubFunction[Int, Int, Int, Int, Int]
      f.when(*, *, *, *).returns(4)
      val memoized = memoizeAsync((a: Int, b: Int, c: Int, d: Int) => Future(f(a, b, c, d)))
      Await.result(memoized(1, 1, 1, 1), timeout) must equal(4)
      Thread.sleep(sleep)
      Await.result(memoized(1, 1, 1, 1), timeout) must equal(4)
      f.verify(*, *, *, *).once()
    }
    "cache no-arg functions with cache name and key" in {
      val f = stubFunction[Int]
      f.when().returns(1)
      def memoized = memoizeAsync("Cache name", "key", () => Future(f()))
      Await.result(memoized(), timeout) must equal(1)
      Thread.sleep(sleep)
      Await.result(memoized(), timeout) must equal(1)
      f.verify().once()
    }
  }

  override protected def beforeEach() {
    cacheManager.clear("")
    cacheManager.clear(specialCacheName)
  }
}
