package com.alexb.infinispan

import com.alexb.memoize.CacheManagerSpec
import org.infinispan.manager.DefaultCacheManager

class InfinispanCacheManagerSpec extends CacheManagerSpec {
	def name = "InfinispanCacheManager"
	
	val manager = new InfinispanCacheManager(new DefaultCacheManager)
	def cacheManager = manager
}
