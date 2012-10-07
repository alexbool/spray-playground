package com.alexb.orders

import akka.actor.Actor
import akka.actor.ActorLogging
import cc.spray.json._
import com.alexb.utils.FutureUtils
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders._

class OrderSearchActor(client: Client, index: String) extends Actor with OrderMarshallers with FutureUtils with ActorLogging {

	def receive = {
		case cmd: SearchOrdersByNotesQuery => answerWithFutureResult { searchByNotes(cmd.query) }
	}
	
	def searchByNotes(query: String) = {
		client.prepareSearch(index)
			.setQuery(fuzzyQuery("notes", query))
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