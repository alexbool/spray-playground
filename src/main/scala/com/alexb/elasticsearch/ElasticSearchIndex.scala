package com.alexb.elasticsearch

import scala.collection.Iterable
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.search.SearchHit

class ElasticSearchIndex(request: SearchRequestBuilder) extends Iterable[SearchHit] {

	val iterator = new LazyIterator(request)
	
	def find(query: QueryBuilder) = new ElasticSearchIndex(request.setQuery(query))
	override def drop(n: Int) = new ElasticSearchIndex(request.setFrom(n))
	override def take(n: Int) = new ElasticSearchIndex(request.setSize(n))
}
