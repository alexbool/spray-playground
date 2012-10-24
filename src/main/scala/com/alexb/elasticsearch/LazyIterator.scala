package com.alexb.elasticsearch

import scala.collection.Iterator
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.search.SearchHit

class LazyIterator(request: SearchRequestBuilder) extends Iterator[SearchHit] {

	var underlying: Option[Iterator[SearchHit]] = None

	def hasNext = {
		evaluate
		underlying.get.hasNext
	}

	def next = {
		evaluate
		underlying.get.next
	}

	private def evaluate {
		if (underlying.isEmpty) {
			underlying = Some(request
				.execute
				.actionGet
				.getHits
				.getHits
				.iterator
			)
		}
	}
}
