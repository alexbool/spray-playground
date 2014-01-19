package com.alexb.cassandra

import scala.concurrent.{Future, Promise}
import com.datastax.driver.core.ResultSet
import com.google.common.util.concurrent.FutureCallback

class FutureListener[T] extends FutureCallback[T] {
  private val promise: Promise[T] = Promise()
  val future: Future[T] = promise.future
  def onSuccess(result: T) = promise.success(result)
  def onFailure(t: Throwable) = promise.failure(t)
}
