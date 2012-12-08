package com.alexb.memoize

import scala.concurrent.{ExecutionContext, Future}

trait Memoize {

  private val defaultCache = ""

  // Synchronous memoize functions
  def memoizeValue[R](key: String, f: => R)(implicit cacheManager: CacheManager): () => R = memoizeValue[R](defaultCache, key, f)
  def memoizeValue[R](cacheName: String, key: String, f: => R)(implicit cacheManager: CacheManager): () => R =
    () => getOrPut(cacheManager.get[R](cacheName, key), cacheManager, f, cacheName, key)

  def memoize[R](f: () => R)(implicit cacheManager: CacheManager): () => R = memoize[R](defaultCache, f)
  def memoize[R](cacheName: String, f: () => R)(implicit cacheManager: CacheManager): () => R = memoize[R](cacheName, f.getClass.getName, f)
  def memoize[R](cacheName: String, key: String, f: () => R)(implicit cacheManager: CacheManager): () => R =
    () => memoize(cacheName, (arg: String) => f())(cacheManager)(key)

  def memoize[A1, R](f: A1 => R)(implicit cacheManager: CacheManager): A1 => R = memoize[A1, R](defaultCache, f)
  def memoize[A1, R](cacheName: String, f: A1 => R)(implicit cacheManager: CacheManager): A1 => R =
    key => getOrPut(cacheManager.get[R](cacheName, key), cacheManager, f(key), cacheName, key)

  def memoize[A1, A2, R](f: (A1, A2) => R)(implicit cacheManager: CacheManager): (A1, A2) => R = memoize[A1, A2, R](defaultCache, f)
  def memoize[A1, A2, R](cacheName: String, f: (A1, A2) => R)(implicit cacheManager: CacheManager): (A1, A2) => R =
    Function.untupled(memoize[(A1, A2), R](cacheName, f.tupled))

  def memoize[A1, A2, A3, R](f: (A1, A2, A3) => R)(implicit cacheManager: CacheManager): (A1, A2, A3) => R = memoize[A1, A2, A3, R](defaultCache, f)
  def memoize[A1, A2, A3, R](cacheName: String, f: (A1, A2, A3) => R)(implicit cacheManager: CacheManager): (A1, A2, A3) => R =
    Function.untupled(memoize[(A1, A2, A3), R](cacheName, f.tupled))

  def memoize[A1, A2, A3, A4, R](f: (A1, A2, A3, A4) => R)(implicit cacheManager: CacheManager): (A1, A2, A3, A4) => R = memoize[A1, A2, A3, A4, R](defaultCache, f)
  def memoize[A1, A2, A3, A4, R](cacheName: String, f: (A1, A2, A3, A4) => R)(implicit cacheManager: CacheManager): (A1, A2, A3, A4) => R =
    Function.untupled(memoize[(A1, A2, A3, A4), R](cacheName, f.tupled))

  private def getOrPut[A, R](res: Option[R], cacheManager: CacheManager, f: => R, cacheName: String, key: A) = res match {
    case Some(value) => value
    case None => {
      val result = f
      cacheManager.put(cacheName, key, result)
      result
    }
  }

  // Async memoize functions
  def memoizeAsyncValue[R](key: String, f: => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): () => Future[R] = memoizeAsyncValue[R](defaultCache, key, f)
  def memoizeAsyncValue[R](cacheName: String, key: String, f: => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): () => Future[R] =
    () => getOrPutAsync(cacheManager, cacheName, key, f)

  def memoizeAsync[R](f: () => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): () => Future[R] = memoizeAsync[R](defaultCache, f)
  def memoizeAsync[R](cacheName: String, f: () => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): () => Future[R] = memoizeAsync[R](cacheName, f.getClass.getName, f)
  def memoizeAsync[R](cacheName: String, key: String, f: () => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): () => Future[R] =
    () => memoizeAsync(cacheName, (arg: String) => f())(cacheManager, ctx)(key)

  def memoizeAsync[A1, R](f: A1 => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): A1 => Future[R] = memoizeAsync[A1, R](defaultCache, f)
  def memoizeAsync[A1, R](cacheName: String, f: A1 => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): A1 => Future[R] =
    key => getOrPutAsync(cacheManager, cacheName, key, f(key))

  def memoizeAsync[A1, A2, R](f: (A1, A2) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2) => Future[R] = memoizeAsync[A1, A2, R](defaultCache, f)
  def memoizeAsync[A1, A2, R](cacheName: String, f: (A1, A2) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2) => Future[R] =
    Function.untupled(memoizeAsync[(A1, A2), R](cacheName, f.tupled))

  def memoizeAsync[A1, A2, A3, R](f: (A1, A2, A3) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2, A3) => Future[R] = memoizeAsync[A1, A2, A3, R](defaultCache, f)
  def memoizeAsync[A1, A2, A3, R](cacheName: String, f: (A1, A2, A3) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2, A3) => Future[R] =
    Function.untupled(memoizeAsync[(A1, A2, A3), R](cacheName, f.tupled))

  def memoizeAsync[A1, A2, A3, A4, R](f: (A1, A2, A3, A4) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2, A3, A4) => Future[R] = memoizeAsync[A1, A2, A3, A4, R](defaultCache, f)
  def memoizeAsync[A1, A2, A3, A4, R](cacheName: String, f: (A1, A2, A3, A4) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2, A3, A4) => Future[R] =
    Function.untupled(memoizeAsync[(A1, A2, A3, A4), R](cacheName, f.tupled))

  private def getOrPutAsync[R](cacheManager: CacheManager, cacheName: String, key: Any, resultFuture: => Future[R])(implicit ctx: ExecutionContext) =
    cacheManager
      .getAsync[R](cacheName, key)
      .flatMap(_ match {
      case Some(value) => Future.successful(value)
      case None => {
        val future = resultFuture
        future onSuccess {
          case result => cacheManager.put(cacheName, key, result)
        }
        future
      }
    })
}

object Memoize extends Memoize
