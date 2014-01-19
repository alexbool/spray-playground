package com.alexb.cassandra

import com.datastax.driver.core.{Session => DatastaxSession, _}
import com.google.common.util.concurrent.{ListenableFuture, Futures}
import scala.concurrent.Future

class Session(underlying: DatastaxSession) {
  def execute(query: String): ResultSet = underlying.execute(query)
  def execute(query: String, values: Any*): ResultSet = underlying.execute(query, values)
  def execute(statement: Statement): ResultSet = underlying.execute(statement)

  def executeAsync(query: String): Future[ResultSet] = translateFuture(underlying.executeAsync(query))
  def executeAsync(query: String, values: Any*): Future[ResultSet] = translateFuture(underlying.executeAsync(query, values))
  def executeAsync(statement: Statement): Future[ResultSet] = translateFuture(underlying.executeAsync(statement))

  def prepare(query: String): PreparedStatement = new PreparedStatement(underlying.prepare(query))
  def prepare(statement: RegularStatement): PreparedStatement = new PreparedStatement(underlying.prepare(statement))

  def shutdown(): Future[Void] = translateFuture(underlying.shutdown())

  def session: DatastaxSession = underlying

  private def translateFuture[T](future: ListenableFuture[T]): Future[T] = {
    val listener = new FutureListener[T]
    Futures.addCallback(future, listener)
    listener.future
  }
}
