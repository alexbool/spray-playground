package com.alexb.main

import com.alexb.infinispan.InfinispanCacheManager
import org.infinispan.manager.DefaultCacheManager
import com.typesafe.config.Config

trait InfinispanContext {
	
	def config: Config
	
	val cacheManager = new InfinispanCacheManager(new DefaultCacheManager(config.getString("infinispan.config")))
}
