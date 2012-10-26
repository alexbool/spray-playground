package com.alexb.main

import com.alexb.infinispan.InfinispanCacheManager
import org.infinispan.manager.DefaultCacheManager
import com.typesafe.config.Config

trait InfinispanContext {
	
	def config: Config
	
	private lazy val cache =
		new InfinispanCacheManager(new DefaultCacheManager(config.getString("infinispan.config")))
	
	def cacheManager = cache
}
