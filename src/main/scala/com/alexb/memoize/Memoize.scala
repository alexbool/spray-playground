package com.alexb.memoize

import scala.concurrent.{ExecutionContext, Future}

trait Memoize {

  private val defaultCache = ""

  // Synchronous memoize functions
  def memoize[R](f: () => R)(implicit cacheManager: CacheManager): () => R = memoize[R](defaultCache, f)
  def memoize[R](cacheName: String, f: () => R)(implicit cacheManager: CacheManager): () => R = memoize[R](cacheName, f.getClass.getName, f)
  def memoize[R](cacheName: String, key: String, f: () => R)(implicit cacheManager: CacheManager): () => R =
    () => memoize(cacheName, (arg: String) => f())(cacheManager)(key)

  def memoize[A1, R](f: A1 => R)(implicit cacheManager: CacheManager): A1 => R = memoize[A1, R](defaultCache, f)
  def memoize[A1, R](cacheName: String, f: A1 => R)(implicit cacheManager: CacheManager): A1 => R =
    key => cacheManager.get[R](cacheName, key) match {
      case Some(value) => value
      case None => {
        val result = f(key)
        cacheManager.put(cacheName, key, result)
        result
      }
    }

  def memoize[A1, A2, R](f: (A1, A2) => R)(implicit cacheManager: CacheManager): (A1, A2) => R = memoize[A1, A2, R](defaultCache, f)
  def memoize[A1, A2, R](cacheName: String, f: (A1, A2) => R)(implicit cacheManager: CacheManager): (A1, A2) => R =
    Function.untupled(memoize[(A1, A2), R](cacheName, f.tupled))

  def memoize[A1, A2, A3, R](f: (A1, A2, A3) => R)(implicit cacheManager: CacheManager): (A1, A2, A3) => R = memoize[A1, A2, A3, R](defaultCache, f)
  def memoize[A1, A2, A3, R](cacheName: String, f: (A1, A2, A3) => R)(implicit cacheManager: CacheManager): (A1, A2, A3) => R =
    Function.untupled(memoize[(A1, A2, A3), R](cacheName, f.tupled))

  def memoize[A1, A2, A3, A4, R](f: (A1, A2, A3, A4) => R)(implicit cacheManager: CacheManager): (A1, A2, A3, A4) => R = memoize[A1, A2, A3, A4, R](defaultCache, f)
  def memoize[A1, A2, A3, A4, R](cacheName: String, f: (A1, A2, A3, A4) => R)(implicit cacheManager: CacheManager): (A1, A2, A3, A4) => R =
    Function.untupled(memoize[(A1, A2, A3, A4), R](cacheName, f.tupled))

  // Async memoize functions
  def memoizeAsync[R](f: () => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): () => Future[R] = memoizeAsync[R](defaultCache, f)
  def memoizeAsync[R](cacheName: String, f: () => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): () => Future[R] = memoizeAsync[R](cacheName, f.getClass.getName, f)
  def memoizeAsync[R](cacheName: String, key: String, f: () => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): () => Future[R] =
    () => memoizeAsync(cacheName, (arg: String) => f())(cacheManager, ctx)(key)

  def memoizeAsync[A1, R](f: A1 => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): A1 => Future[R] = memoizeAsync[A1, R](defaultCache, f)
  def memoizeAsync[A1, R](cacheName: String, f: A1 => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): A1 => Future[R] =
    key => cacheManager
      .getAsync[R](cacheName, key)
      .flatMap(_ match {
        case Some(value) => Future.successful(value)
        case None => {
          val future = f(key)
          future onSuccess {
            case result => cacheManager.put(cacheName, key, result)
          }
          future
        }
      })

  def memoizeAsync[A1, A2, R](f: (A1, A2) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2) => Future[R] = memoizeAsync[A1, A2, R](defaultCache, f)
  def memoizeAsync[A1, A2, R](cacheName: String, f: (A1, A2) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2) => Future[R] =
    Function.untupled(memoizeAsync[(A1, A2), R](cacheName, f.tupled))

  def memoizeAsync[A1, A2, A3, R](f: (A1, A2, A3) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2, A3) => Future[R] = memoizeAsync[A1, A2, A3, R](defaultCache, f)
  def memoizeAsync[A1, A2, A3, R](cacheName: String, f: (A1, A2, A3) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2, A3) => Future[R] =
    Function.untupled(memoizeAsync[(A1, A2, A3), R](cacheName, f.tupled))

  def memoizeAsync[A1, A2, A3, A4, R](f: (A1, A2, A3, A4) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2, A3, A4) => Future[R] = memoizeAsync[A1, A2, A3, A4, R](defaultCache, f)
  def memoizeAsync[A1, A2, A3, A4, R](cacheName: String, f: (A1, A2, A3, A4) => Future[R])(implicit cacheManager: CacheManager, ctx: ExecutionContext): (A1, A2, A3, A4) => Future[R] =
    Function.untupled(memoizeAsync[(A1, A2, A3, A4), R](cacheName, f.tupled))
}

object Memoize extends Memoize
