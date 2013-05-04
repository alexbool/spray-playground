package com.alexb.zk

import org.scalatest.{Ignore, BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import org.apache.zookeeper.{CreateMode, ZooKeeper}
import com.alexb.test.Config
import org.apache.zookeeper.ZooDefs.Ids

@Ignore
class ZkPropertiesSpec extends WordSpec with BeforeAndAfterAll with MustMatchers with Config {
  val connectString = config.getString("zk.connectString")
  val root = "/test"
  val zkSessionTimeoutMillis = 12000
  // Make it not lazy when you want to run the test
  lazy val zk = new ZooKeeper(connectString, zkSessionTimeoutMillis, null)
  lazy val zkProperties = new ZkProperties(connectString + root, zkSessionTimeoutMillis)
  val acl = Ids.OPEN_ACL_UNSAFE
  val sleepMillis = 500

  // Uncomment when you want to run the test
  /* override def beforeAll() {
    zk.create(root, new Array[Byte](0), acl, CreateMode.PERSISTENT)
  }

  override def afterAll() {
    zk.delete(s"$root/prop2", -1)
    zk.delete(root, -1)
    zk.close()
    zkProperties.close()
  } */

  "ZkProperties" must {
    "get properties" in {
      zk.create(s"$root/prop1", "data".getBytes, acl, CreateMode.PERSISTENT)
      Thread.sleep(sleepMillis)
      zkProperties("prop1") must be (Some("data"))
    }
    "update properties" in {
      zk.setData(s"$root/prop1", "new_data".getBytes, -1)
      Thread.sleep(sleepMillis)
      zkProperties("prop1") must be (Some("new_data"))
    }
    "get newly created properties" in {
      zk.create(s"$root/prop2", "some_data".getBytes, acl, CreateMode.PERSISTENT)
      Thread.sleep(sleepMillis)
      zkProperties("prop2") must be (Some("some_data"))
    }
    "create, update and remove properties" in {
      zkProperties.put("prop3", true)
      Thread.sleep(sleepMillis)
      zkProperties.getBoolean("prop3") must be (Some(true))

      zkProperties.put("prop3", false)
      Thread.sleep(sleepMillis)
      zkProperties.getBoolean("prop3") must be (Some(false))

      zkProperties.remove("prop3")
      Thread.sleep(sleepMillis)
      zkProperties.getBoolean("prop3") must be (None)
    }
    "delete removed properties" in {
      zk.delete(s"$root/prop1", -1)
      Thread.sleep(sleepMillis)
      zkProperties("prop1") must be (None)
    }
  }
}
