package com.alexb.memoize

trait CacheManager {
  def get[T](cacheName: String, key: Any): Option[T]
  def put(cacheName: String, key: Any, value: Any)
}
