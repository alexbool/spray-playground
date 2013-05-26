package com.alexb.main

import scala.concurrent.duration._
import akka.util.Timeout
import com.alexb.calculator.CalculatorService
import com.alexb.orders.OrderService
import com.alexb.statics.StaticsService
import com.alexb.user.UserService
import spray.routing.HttpServiceActor
import language.postfixOps

class SprayPlaygroundActor(calculatorService: CalculatorService, orderService: OrderService,
                           staticsService: StaticsService, userService: UserService)
  extends HttpServiceActor {

  val timeout: Timeout = 5 seconds // needed for `?`

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(calculatorService.calculatorRoute ~
    orderService.orderRoute ~
    staticsService.staticsRoute ~
    userService.userRoute)
}
