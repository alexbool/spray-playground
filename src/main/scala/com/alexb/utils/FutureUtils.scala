package com.alexb.utils

import akka.actor.Actor
import scala.concurrent.Future

trait FutureUtils {
  this: Actor =>

  import context.dispatcher

  def wrapInFuture[T](body: => T) {
    Future {
      body
    }
  }

  def answerWithFutureResult[T](body: => T) {
    val originalSender = sender
    Future {
      body
    } onSuccess {
      case result => originalSender ! result
    }
  }
}