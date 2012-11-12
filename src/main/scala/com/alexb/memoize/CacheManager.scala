package com.alexb.memoize

trait CacheManager {
  def get(cacheName: String, key: Any): Option[Any]
  def put(cacheName: String, key: Any, value: Any)
}
