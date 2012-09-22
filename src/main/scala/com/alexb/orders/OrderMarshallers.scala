package com.alexb.orders

import cc.spray.http._
import cc.spray.typeconversion._
import cc.spray.http.MediaTypes._
import cc.spray.json.DefaultJsonProtocol

trait OrderMarshallers extends DefaultJsonProtocol {
	implicit val orderItemFormat = jsonFormat2(OrderItem)
	implicit val orderFormat = jsonFormat3(Order)
	
	implicit val addOrderCommandFormat = jsonFormat2(AddOrderCommand)
}
