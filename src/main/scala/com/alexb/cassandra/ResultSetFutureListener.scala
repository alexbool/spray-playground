package com.alexb.cassandra

import scala.concurrent.{Future, Promise}
import com.datastax.driver.core.ResultSet
import com.google.common.util.concurrent.FutureCallback

class ResultSetFutureListener extends FutureCallback[ResultSet] {
  private val promise: Promise[ResultSet] = Promise()
  val future: Future[ResultSet] = promise.future
  def onSuccess(result: ResultSet) = promise.success(result)
  def onFailure(t: Throwable) = promise.failure(t)
}
