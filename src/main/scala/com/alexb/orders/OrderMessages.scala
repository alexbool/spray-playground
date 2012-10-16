package com.alexb.orders

import com.alexb.utils.PageInfo

case class AddOrderCommand(clientId: String, items: List[OrderItem], notes: String)
case class DeleteOrderCommand(orderId: String)

case class OrderByIdQuery(orderId: String) 
case class OrdersByClientIdQuery(clientId: String, page: PageInfo)

case class SearchOrdersByNotesQuery(query: String, page: PageInfo)
