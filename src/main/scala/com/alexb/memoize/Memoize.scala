package com.alexb.memoize

trait Memoize {

	def memoize[A1, R](f: A1 => R)(implicit cacheManager: CacheManager): A1 => R = memoize()(f)

	def memoize[A1, R](cacheName: String = "")(f: A1 => R)(implicit cacheManager: CacheManager): A1 => R =
		a1 => cacheManager.get(cacheName, a1) match {
			case Some(value) => value.asInstanceOf[R]
			case None => {
				val result = f(a1)
				cacheManager.put(cacheName, a1, result)
				result
			}
		}
}

object Memoize extends Memoize
