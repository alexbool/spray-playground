package com.alexb.zk

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import org.apache.zookeeper.{CreateMode, ZooKeeper}
import com.alexb.test.Config
import org.apache.zookeeper.ZooDefs.Ids

class ZkPropertiesSpec extends WordSpec with BeforeAndAfterAll with MustMatchers with Config {

  val connectString = config.getString("zk.connectString")
  val root = "/test"
  val zk = new ZooKeeper(connectString, 1000000, null)
  val zkProperties = new ZkProperties(connectString + root, 1000000)
  val acl = Ids.OPEN_ACL_UNSAFE
  val timeout = 500

  override def beforeAll() {
    zk.create(root, new Array[Byte](0), acl, CreateMode.PERSISTENT)
  }

  override def afterAll() {
    zk.delete(root, -1)
    zk.close()
    zkProperties.close()
  }

  "ZkProperties" ignore {
    "get properties" in {
      zk.create(s"$root/prop1", "data".getBytes, acl, CreateMode.PERSISTENT)
      Thread.sleep(timeout)
      zkProperties("prop1") must be (Some("data"))
    }
    "update properties" in {
      zk.setData(s"$root/prop1", "new_data".getBytes, -1)
      Thread.sleep(timeout)
      zkProperties("prop1") must be (Some("new_data"))
    }
    "delete removed properties" in {
      zk.delete(s"$root/prop1", -1)
      Thread.sleep(timeout)
      zkProperties("prop1") must be (None)
    }
  }
}
