package com.alexb.cassandra

import com.datastax.driver.core.{PreparedStatement => DatastaxPreparedStatement, _}
import com.datastax.driver.core.policies.RetryPolicy
import java.nio.ByteBuffer

class PreparedStatement(underlying: DatastaxPreparedStatement) {
  def bind(values: Any*): BoundStatement = underlying.bind(values.map(_.asInstanceOf[AnyRef]): _*)

  def variables: ColumnDefinitions = underlying.getVariables

  def disableTracing() { underlying.disableTracing() }
  def enableTracing() { underlying.enableTracing() }
  def isTracing: Boolean = underlying.isTracing

  def consistencyLevel: ConsistencyLevel = underlying.getConsistencyLevel
  def consistencyLevel_=(consistency: ConsistencyLevel) { underlying.setConsistencyLevel(consistency) }

  def queryKeyspace: String = underlying.getQueryKeyspace
  def queryString: String = underlying.getQueryString

  def retryPolicy: RetryPolicy = underlying.getRetryPolicy
  def retryPolicy_=(retryPolicy: RetryPolicy) { underlying.setRetryPolicy(retryPolicy) }

  def routingKey_=(routingKey: ByteBuffer) { underlying.setRoutingKey(routingKey) }
  def routingKey_=(routingKeyComponents: ByteBuffer*) { underlying.setRoutingKey(routingKeyComponents: _*) }
}
