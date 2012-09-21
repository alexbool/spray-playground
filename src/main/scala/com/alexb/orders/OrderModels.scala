package com.alexb.orders

case class Order(orderId: String, clientId: String, items: List[OrderItem])
case class OrderItem(itemId: String, quantity: Int)
