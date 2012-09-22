package com.alexb.orders

case class AddOrderCommand(clientId: String, items: List[OrderItem])
case class DeleteOrderCommand(orderId: String)

case class OrdersByClientIdQuery(clientId: String)
