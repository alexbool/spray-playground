package com.alexb.cassandra

import org.scalatest.{BeforeAndAfterAll, WordSpec, Matchers}
import com.datastax.driver.core.Cluster
import scala.concurrent.Await
import scala.concurrent.duration._
import java.lang.Long

import language.postfixOps

class SessionSpec extends WordSpec with Matchers with BeforeAndAfterAll {
  val cluster = Cluster.builder().addContactPoint("localhost").build()
  val session = new Session(cluster.connect())
  val timeout = 10 seconds

  "Cassandra client" should {
    "be able to create keyspaces and tables" in {
      val ddl =
      """
        |CREATE KEYSPACE test WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};
        |
        |USE test;
        |
        |CREATE TABLE history (
        |    uid bigint,
        |    timestamp bigint,
        |    track_id int,
        |    album_id int,
        |    PRIMARY KEY (uid, timestamp)
        |);
      """.stripMargin
      val statements = ddl.split(";").map(_.trim).filter(_.length > 0)
      statements foreach { stmt =>
        session.execute(stmt)
      }
    }
    "create and use preapred statements" in {
      val prepared = session.prepare("INSERT INTO history (uid, timestamp, track_id, album_id) VALUES (?, ?, ?, ?)")
      val binded = prepared.bind(new Long(1), new Long(2), new Integer(3), new Integer(4)) // XXX That's ugly
      val future = session.executeAsync(binded)
      Await.result(future, timeout)
    }
    "perform async operations" in {
      val future = session.executeAsync("SELECT * FROM history")
      val resultSet = Await.result(future, timeout)
      val results = resultSet.all()
      results.size() should be (1)
      val result = results.get(0)
      result.getLong("uid") should be (1)
      result.getLong("timestamp") should be (2)
      result.getInt("track_id") should be (3)
      result.getInt("album_id") should be (4)
    }
    "clean up" in {
      session.execute("DROP KEYSPACE test")
    }
  }

  override protected def afterAll() {
    Await.ready(session.shutdown(), timeout)
    cluster.shutdown().get()
    super.afterAll()
  }
}
