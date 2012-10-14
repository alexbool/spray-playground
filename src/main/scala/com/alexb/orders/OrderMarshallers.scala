package com.alexb.orders

import spray.http._
import spray.httpx._
import spray.http.MediaTypes._
import spray.json.DefaultJsonProtocol

trait OrderMarshallers extends DefaultJsonProtocol {
	implicit val orderItemFormat = jsonFormat2(OrderItem)
	implicit val orderFormat = jsonFormat4(Order)
	
	implicit val addOrderCommandFormat = jsonFormat3(AddOrderCommand)
}
