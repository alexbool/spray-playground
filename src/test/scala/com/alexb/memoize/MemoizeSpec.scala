package com.alexb.memoize

import org.scalatest._
import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import Memoize._
import language.postfixOps

class MemoizeSpec extends WordSpec with Matchers with BeforeAndAfterEach {

	implicit val cacheManager = new ConcurrentHashMapCacheManager
  val specialCacheName = "other cache"
  val timeout = 10 seconds
  val sleep = 10

	"Memoize function" should {
		"cache by-name value" in {
      val m = stubFunction0(1)
			val memoized = memoizeValue("a key", m())
			memoized() should equal(1)
			memoized() should equal(1)
		}
    "cache no-arg functions" in {
      val m = stubFunction0(1)
      val memoized = memoize(m)
      memoized() should equal(1)
      memoized() should equal(1)
    }
		"cache one-arg functions" in {
      val m = stubFunction1(2)
      val memoized = memoize(m)
			memoized(1) should equal(2)
			memoized(1) should equal(2)
		}
		"cache two-arg functions" in {
      val m = stubFunction2(3)
      val memoized = memoize(m)
			memoized(1, 2) should equal(3)
			memoized(1, 2) should equal(3)
		}
		"cache three-arg functions" in {
      val m = stubFunction3(3)
      val memoized = memoize(m)
			memoized(1, 1, 1) should equal(3)
			memoized(1, 1, 1) should equal(3)
		}
		"cache four-arg functions" in {
      val m = stubFunction4(4)
      val memoized = memoize(m)
			memoized(1, 1, 1, 1) should equal(4)
			memoized(1, 1, 1, 1) should equal(4)
		}
		"cache no-arg functions with cache name and key" in {
      val m = stubFunction0(1)
			def memoized = memoize(specialCacheName, "key", m)
			memoized() should equal(1)
			memoized() should equal(1)
		}
	}

  "Async memoize function" should {
    "cache by-name values" in {
      val f = stubFunction0(1)
      val memoized = memoizeAsyncValue("a key", Future(f()))
      Await.result(memoized(), timeout) should equal(1)
      Thread.sleep(sleep)
      Await.result(memoized(), timeout) should equal(1)
    }
    "cache no-arg functions" in {
      val f = stubFunction0(1)
      val memoized = memoizeAsync(() => Future(f()))
      Await.result(memoized(), timeout) should equal(1)
      Thread.sleep(sleep)
      Await.result(memoized(), timeout) should equal(1)
    }
    "cache one-arg functions" in {
      val f = stubFunction1(2)
      val memoized = memoizeAsync((a: Int) => Future(f(a)))
      Await.result(memoized(1), timeout) should equal(2)
      Thread.sleep(sleep)
      Await.result(memoized(1), timeout) should equal(2)
    }
    "cache two-arg functions" in {
      val f = stubFunction2(3)
      val memoized = memoizeAsync((a: Int, b: Int) => Future(f(a, b)))
      Await.result(memoized(1, 2), timeout) should equal(3)
      Thread.sleep(sleep)
      Await.result(memoized(1, 2), timeout) should equal(3)
    }
    "cache three-arg functions" in {
      val f = stubFunction3(3)
      val memoized = memoizeAsync((a: Int, b: Int, c: Int) => Future(f(a, b, c)))
      Await.result(memoized(1, 1, 1), timeout) should equal(3)
      Thread.sleep(sleep)
      Await.result(memoized(1, 1, 1), timeout) should equal(3)
    }
    "cache four-arg functions" in {
      val f = stubFunction4(4)
      val memoized = memoizeAsync((a: Int, b: Int, c: Int, d: Int) => Future(f(a, b, c, d)))
      Await.result(memoized(1, 1, 1, 1), timeout) should equal(4)
      Thread.sleep(sleep)
      Await.result(memoized(1, 1, 1, 1), timeout) should equal(4)
    }
    "cache no-arg functions with cache name and key" in {
      val f = stubFunction0(1)
      def memoized = memoizeAsync("Cache name", "key", () => Future(f()))
      Await.result(memoized(), timeout) should equal(1)
      Thread.sleep(sleep)
      Await.result(memoized(), timeout) should equal(1)
    }
  }

  override protected def beforeEach() {
    cacheManager.clear("")
    cacheManager.clear(specialCacheName)
  }

  trait CalledOnlyOnce {
    private var called = false

    def incOrThrow() {
      this.synchronized {
        if (!called)
          called = true
        else
          throw new IllegalStateException
      }
    }
  }

  def stubFunction0(result: Int) = new Function0[Int] with CalledOnlyOnce {
    def apply(): Int = {
      incOrThrow()
      result
    }
  }

  def stubFunction1(result: Int) = new Function1[Int, Int] with CalledOnlyOnce {
    def apply(v1: Int): Int = {
      incOrThrow()
      result
    }
  }

  def stubFunction2(result: Int) = new Function2[Int, Int, Int] with CalledOnlyOnce {
    def apply(v1: Int, v2: Int): Int = {
      incOrThrow()
      result
    }
  }

  def stubFunction3(result: Int) = new Function3[Int, Int, Int, Int] with CalledOnlyOnce {
    def apply(v1: Int, v2: Int, v3: Int): Int = {
      incOrThrow()
      result
    }
  }

  def stubFunction4(result: Int) = new Function4[Int, Int, Int, Int, Int] with CalledOnlyOnce {
    def apply(v1: Int, v2: Int, v3: Int, v4: Int): Int = {
      incOrThrow()
      result
    }
  }
}
