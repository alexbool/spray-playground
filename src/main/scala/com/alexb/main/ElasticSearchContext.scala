package com.alexb.main

import com.typesafe.config.Config
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

trait ElasticSearchContext {
	
	def config: Config

	val elasticSearchClient: Client =
		new TransportClient().addTransportAddress(new InetSocketTransportAddress(config.getString("elasticsearch.host"), 9300))
}