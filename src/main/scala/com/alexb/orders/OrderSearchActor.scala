package com.alexb.orders

import akka.actor.Actor
import akka.actor.ActorLogging
import spray.json._
import com.alexb.utils.{ FutureUtils, PageInfo }
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders._

class OrderSearchActor(client: Client, index: String) extends Actor with OrderMarshallers with FutureUtils with ActorLogging {

	def receive = {
		case cmd: SearchOrdersByNotesQuery => answerWithFutureResult { searchByNotes(cmd.query, cmd.page) }
	}
	
	def searchByNotes(query: String, page: PageInfo) = {
		client.prepareSearch(index)
			.setQuery(fuzzyQuery("notes", query))
			.setFrom(page.skip)
			.setSize(page.size)
			.execute()
			.actionGet()
			.getHits()
			.getHits()
			.map(h => h.getSourceAsString()
					.replace("\"_id\"", "\"orderId\"") // Bad hack
					.asJson.convertTo[Order])
			.toSeq
	}
}
