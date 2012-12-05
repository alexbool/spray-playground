package com.alexb.infinispan

import com.alexb.memoize.CacheManager
import org.infinispan.api.BasicCacheContainer
import org.infinispan.util.concurrent.FutureListener
import scala.concurrent.{Promise, ExecutionContext}
import scala.util.Try

class InfinispanCacheManager(cacheContainer: BasicCacheContainer) extends CacheManager {

  private class InfinispanFutureListener[T](promise: Promise[Option[T]]) extends FutureListener[T] {
    def futureDone(future: java.util.concurrent.Future[T]) {
      promise.tryComplete(Try(Option(future.get())))
    }
  }

  private def cache[T](cacheName: String) = cacheContainer.getCache[Any, T](cacheName)

  def get[T](cacheName: String, key: Any): Option[T] =
    Option(cache(cacheName).get(key))

  override def getAsync[T](cacheName: String, key: Any)(implicit ctx: ExecutionContext) = {
    val promise = Promise[Option[T]]()
    cache(cacheName).getAsync(key).attachListener(new InfinispanFutureListener(promise))
    promise.future
  }

  def put(cacheName: String, key: Any, value: Any) {
    cacheContainer.getCache(cacheName).put(key, value)
  }
}
