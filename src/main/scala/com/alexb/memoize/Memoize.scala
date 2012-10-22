package com.alexb.memoize

trait Memoize {

	private val defaultName = ""
	
	def memoize[A1, R](f: A1 => R)(implicit cacheManager: CacheManager): A1 => R = memoize[A1, R](defaultName)(f)
	def memoize[A1, R](cacheName: String)(f: A1 => R)(implicit cacheManager: CacheManager): A1 => R =
		a1 => cacheManager.get(cacheName, a1) match {
			case Some(value) => value.asInstanceOf[R]
			case None => {
				val result = f(a1)
				cacheManager.put(cacheName, a1, result)
				result
			}
		}
	
	def memoize[A1, A2, R](f: (A1, A2) => R)(implicit cacheManager: CacheManager): (A1, A2) => R = memoize[A1, A2, R](defaultName)(f)
	def memoize[A1, A2, R](cacheName: String)(f: (A1, A2) => R)(implicit cacheManager: CacheManager): (A1, A2) => R =
		Function.untupled(memoize[(A1, A2), R](cacheName)(f.tupled))
	
	def memoize[A1, A2, A3, R](f: (A1, A2, A3) => R)(implicit cacheManager: CacheManager): (A1, A2, A3) => R = memoize[A1, A2, A3, R](defaultName)(f)
	def memoize[A1, A2, A3, R](cacheName: String)(f: (A1, A2, A3) => R)(implicit cacheManager: CacheManager): (A1, A2, A3) => R =
		Function.untupled(memoize[(A1, A2, A3), R](cacheName)(f.tupled))
	
	def memoize[A1, A2, A3, A4, R](f: (A1, A2, A3, A4) => R)(implicit cacheManager: CacheManager): (A1, A2, A3, A4) => R = memoize[A1, A2, A3, A4, R](defaultName)(f)
	def memoize[A1, A2, A3, A4, R](cacheName: String)(f: (A1, A2, A3, A4) => R)(implicit cacheManager: CacheManager): (A1, A2, A3, A4) => R =
		Function.untupled(memoize[(A1, A2, A3, A4), R](cacheName)(f.tupled))
}

object Memoize extends Memoize
