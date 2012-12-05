package com.alexb.infinispan

import com.alexb.memoize.CacheManager
import org.infinispan.api.BasicCacheContainer

class InfinispanCacheManager(cacheContainer: BasicCacheContainer) extends CacheManager {

  def get[T](cacheName: String, key: Any): Option[T] =
    Option(cacheContainer.getCache(cacheName).get(key).asInstanceOf[T])

  def put(cacheName: String, key: Any, value: Any) {
    cacheContainer.getCache(cacheName).put(key, value)
  }
}
