package com.alexb.memoize

import concurrent.{ExecutionContext, Future}

trait CacheManager {
  def get[T](cacheName: String, key: Any): Option[T]

  // A very basic implementation, override if your cache has async nature
  def getAsync[T](cacheName: String, key: Any)(implicit ctx: ExecutionContext): Future[Option[T]] =
    Future { get[T](cacheName, key) }

  def put(cacheName: String, key: Any, value: Any)
}
