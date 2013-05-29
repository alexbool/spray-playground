package com.alexb.memoize

import com.alexb.memoize.Memoize._
import scala.concurrent.{ExecutionContext, Future}

object Implicits {

  implicit class MemoizableValue[R](f: => R) {
    def memoized(key: String)(implicit cm: CacheManager)                    = memoizeValue(key, f)
    def memoized(cacheName: String, key: String)(implicit cm: CacheManager) = memoizeValue(cacheName, key, f)
  }

  implicit class MemoizableFunction0[R](f: () => R) {
    def memoized(implicit cm: CacheManager)                                 = memoize(f)
    def memoized(cacheName: String)(implicit cm: CacheManager)              = memoize(cacheName, f)
    def memoized(cacheName: String, key: String)(implicit cm: CacheManager) = memoize(cacheName, key, f)
  }

  implicit class MemoizableFunction1[A, R](f: A => R) {
    def memoized(implicit cm: CacheManager)                    = memoize(f)
    def memoized(cacheName: String)(implicit cm: CacheManager) = memoize(cacheName, f)
  }

  implicit class MemoizableFunction2[A1, A2, R](f: (A1, A2) => R) {
    def memoized(implicit cm: CacheManager)                    = memoize(f)
    def memoized(cacheName: String)(implicit cm: CacheManager) = memoize(cacheName, f)
  }

  implicit class MemoizableFunction3[A1, A2, A3, R](f: (A1, A2, A3) => R) {
    def memoized(implicit cm: CacheManager)                    = memoize(f)
    def memoized(cacheName: String)(implicit cm: CacheManager) = memoize(cacheName, f)
  }

  implicit class MemoizableFunction4[A1, A2, A3, A4, R](f: (A1, A2, A3, A4) => R) {
    def memoized(implicit cm: CacheManager)                    = memoize(f)
    def memoized(cacheName: String)(implicit cm: CacheManager) = memoize(cacheName, f)
  }

  implicit class AsyncMemoizableValue[R](f: => Future[R]) {
    def memoizedAsync(key: String)(implicit cm: CacheManager, ec: ExecutionContext)                    = memoizeAsyncValue(key, f)
    def memoizedAsync(cacheName: String, key: String)(implicit cm: CacheManager, ec: ExecutionContext) = memoizeAsyncValue(cacheName, key, f)
  }

  implicit class AsyncMemoizableFunction0[R](f: () => Future[R]) {
    def memoizedAsync(implicit cm: CacheManager, ec: ExecutionContext)                                 = memoizeAsync(f)
    def memoizedAsync(cacheName: String)(implicit cm: CacheManager, ec: ExecutionContext)              = memoizeAsync(cacheName, f)
    def memoizedAsync(cacheName: String, key: String)(implicit cm: CacheManager, ec: ExecutionContext) = memoizeAsync(cacheName, key, f)
  }

  implicit class AsyncMemoizableFunction1[A, R](f: A => Future[R]) {
    def memoizedAsync(implicit cm: CacheManager, ec: ExecutionContext)                    = memoizeAsync(f)
    def memoizedAsync(cacheName: String)(implicit cm: CacheManager, ec: ExecutionContext) = memoizeAsync(cacheName, f)
  }

  implicit class AsyncMemoizableFunction2[A1, A2, R](f: (A1, A2) => Future[R]) {
    def memoizedAsync(implicit cm: CacheManager, ec: ExecutionContext)                    = memoizeAsync(f)
    def memoizedAsync(cacheName: String)(implicit cm: CacheManager, ec: ExecutionContext) = memoizeAsync(cacheName, f)
  }

  implicit class AsyncMemoizableFunction3[A1, A2, A3, R](f: (A1, A2, A3) => Future[R]) {
    def memoizedAsync(implicit cm: CacheManager, ec: ExecutionContext)                    = memoizeAsync(f)
    def memoizedAsync(cacheName: String)(implicit cm: CacheManager, ec: ExecutionContext) = memoizeAsync(cacheName, f)
  }

  implicit class AsyncMemoizableFunction4[A1, A2, A3, A4, R](f: (A1, A2, A3, A4) => Future[R]) {
    def memoizedAsync(implicit cm: CacheManager, ec: ExecutionContext)                    = memoizeAsync(f)
    def memoizedAsync(cacheName: String)(implicit cm: CacheManager, ec: ExecutionContext) = memoizeAsync(cacheName, f)
  }
}
