package com.alexb.elasticsearch

import org.elasticsearch.client.Client

trait ElasticSearch {

  def index(index: String)(implicit client: Client) =
    new ElasticSearchIndex(client.prepareSearch(index))
}

object ElasticSearch extends ElasticSearch
