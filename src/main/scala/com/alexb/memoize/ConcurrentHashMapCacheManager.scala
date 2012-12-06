package com.alexb.memoize

import java.util.concurrent.ConcurrentHashMap

class ConcurrentHashMapCacheManager extends CacheManager {

  private val caches = new ConcurrentHashMap[String, ConcurrentHashMap[Any, Any]]

  def get[T](cacheName: String, key: Any): Option[T] = {
    val cache = getOrCreateCache(cacheName)
    System.out.println(s"Cache '$cacheName' contents: " + Map(cache))
    if (cache.containsKey(key)) Some(cache.get(key).asInstanceOf[T])
    else None
  }

  def put(cacheName: String, key: Any, value: Any) {
    getOrCreateCache(cacheName).put(key, value)
  }

  def clear(cacheName: String) {
    Option(caches.get(cacheName)) foreach { _.clear() }
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
