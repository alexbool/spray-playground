package com.alexb.orders

case class AddOrderCommand(clientId: String, items: List[OrderItem], notes: String)
case class DeleteOrderCommand(orderId: String)

case class OrderByIdQuery(orderId: String)
case class OrdersByClientIdQuery(clientId: String)

case class SearchOrdersByNotesQuery(query: String)
