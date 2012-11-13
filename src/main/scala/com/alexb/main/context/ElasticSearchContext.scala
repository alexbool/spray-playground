package com.alexb.main
package context

import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

trait ElasticSearchContext {
  this: Configuration =>

  val elasticSearchClient: Client =
    new TransportClient().addTransportAddress(new InetSocketTransportAddress(config.getString("elasticsearch.host"), 9300))
}