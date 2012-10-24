package com.alexb.orders

import akka.actor.Actor
import akka.actor.ActorLogging
import spray.json._
import com.alexb.elasticsearch.ElasticSearch
import com.alexb.utils.{ FutureUtils, PageInfo }
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders._

class OrderSearchActor(client: Client, index: String) extends Actor with ElasticSearch with OrderMarshallers with FutureUtils with ActorLogging {

	implicit val esClient = client
	
	def receive = {
		case cmd: SearchOrdersByNotesQuery => answerWithFutureResult { searchByNotes(cmd.query, cmd.page) }
	}
	
	def searchByNotes(query: String, page: PageInfo) = 
		index(index)
			.find(fuzzyQuery("notes", query))
			.drop(page.skip)
			.take(page.size)
			.map(_.getSourceAsString()
					.replace("\"_id\"", "\"orderId\"") // Bad hack
					.asJson.convertTo[Order])
			.toSeq
}
