package com.alexb.orders

case class AddOrderCommand(clientId: String)
case class DeleteOrderCommand(orderId: String)
