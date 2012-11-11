package com.alexb.main
package context

import com.alexb.infinispan.InfinispanCacheManager
import org.infinispan.manager.DefaultCacheManager

trait InfinispanContext {
	this: ConfigContext =>
	
	val cacheManager = new InfinispanCacheManager(new DefaultCacheManager(config.getString("infinispan.config")))
}
