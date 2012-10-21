package com.alexb.infinispan

import com.alexb.memoize.CacheManager
import org.infinispan.api.BasicCacheContainer

class InfinispanCacheManager(cacheContainer: BasicCacheContainer) extends CacheManager {

	def get(cacheName: String, key: Any): Option[Any] =
		Option(cacheContainer.getCache(cacheName).get(key))

	def put(cacheName: String, key: Any, value: Any) {
		cacheContainer.getCache(cacheName).put(key, value)
	}
}
