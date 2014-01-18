package com.alexb.cassandra

import com.datastax.driver.core.{Session => DatastaxSession, _}
import com.google.common.util.concurrent.Futures
import scala.concurrent.Future

class Session(underlying: DatastaxSession) {
  def execute(query: String): ResultSet = underlying.execute(query)
  def execute(query: String, values: Any*): ResultSet = underlying.execute(query, values)
  def execute(statement: Statement): ResultSet = underlying.execute(statement)

  private def translateFuture(future: ResultSetFuture): Future[ResultSet] = {
    val listener = new ResultSetFutureListener
    Futures.addCallback(future, listener)
    listener.future
  }

  def executeAsync(query: String): Future[ResultSet] = translateFuture(underlying.executeAsync(query))
  def executeAsync(query: String, values: Any*): Future[ResultSet] = translateFuture(underlying.executeAsync(query, values))
  def executeAsync(statement: Statement): Future[ResultSet] = translateFuture(underlying.executeAsync(statement))

  def prepare(query: String): PreparedStatement = underlying.prepare(query)
  def prepare(statement: RegularStatement): PreparedStatement = underlying.prepare(statement)

  def shutdown(): ShutdownFuture = underlying.shutdown()

  def session: DatastaxSession = underlying
}
