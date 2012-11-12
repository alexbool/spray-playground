package com.alexb.orders

import spray.json.DefaultJsonProtocol

trait OrderMarshallers extends DefaultJsonProtocol {
  implicit val orderItemFormat = jsonFormat2(OrderItem)
  implicit val orderFormat = jsonFormat4(Order)

  implicit val addOrderCommandFormat = jsonFormat3(AddOrderCommand)
}
