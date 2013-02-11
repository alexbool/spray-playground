package com.alexb.main
package context

import org.elasticsearch.client.Client

trait ElasticSearchSupport {
  def elasticSearchClient: Client
}
