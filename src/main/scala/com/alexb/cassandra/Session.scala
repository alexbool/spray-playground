package com.alexb.cassandra

import com.datastax.driver.core.{Session => DatastaxSession, _}
import scala.concurrent.Future
import Futures._

class Session(underlying: DatastaxSession) {
  def execute(query: String): ResultSet = underlying.execute(query)
  def execute(query: String, values: Any*): ResultSet = underlying.execute(query, values.map(_.asInstanceOf[AnyRef]): _*)
  def execute(statement: Statement): ResultSet = underlying.execute(statement)

  def executeAsync(query: String): Future[ResultSet] = fromListenableFuture(underlying.executeAsync(query))
  def executeAsync(query: String, values: Any*): Future[ResultSet] = fromListenableFuture(underlying.executeAsync(query, values.map(_.asInstanceOf[AnyRef]): _*))
  def executeAsync(statement: Statement): Future[ResultSet] = fromListenableFuture(underlying.executeAsync(statement))

  def prepare(query: String): PreparedStatement = new PreparedStatement(underlying.prepare(query))
  def prepare(statement: RegularStatement): PreparedStatement = new PreparedStatement(underlying.prepare(statement))

  def close(): Unit = underlying.close()
  def closeAsync(): Future[Unit] = fromListenableFuture(underlying.closeAsync()).asInstanceOf[Future[Unit]]

  def session: DatastaxSession = underlying
}
