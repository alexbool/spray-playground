package com.alexb.memoize

import java.util.concurrent.ConcurrentHashMap

class ConcurrentHashMapCacheManager extends CacheManager {

  private val caches = new ConcurrentHashMap[String, ConcurrentHashMap[Any, Any]]

  def get(cacheName: String, key: Any): Option[Any] = {
    val cache = getOrCreateCache(cacheName)
    if (cache.containsKey(key)) Some(cache.get(key))
    else None
  }

  def put(cacheName: String, key: Any, value: Any) {
    getOrCreateCache(cacheName).put(key, value)
  }

  private def getOrCreateCache(cacheName: String) =
    if (caches.containsKey(cacheName))
      caches.get(cacheName)
    else {
      val newCache = new ConcurrentHashMap[Any, Any]
      caches.put(cacheName, newCache)
      newCache
    }
}
