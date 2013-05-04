package com.alexb.zk

import org.apache.zookeeper.{AsyncCallback, WatchedEvent, Watcher, ZooKeeper}
import org.apache.zookeeper.Watcher.Event.{KeeperState, EventType}
import org.apache.zookeeper.data.Stat
import java.io.Closeable
import scala.collection.JavaConversions._

class ZkProperties(connectString: String, sessionTimeout: Int, readOnly: Boolean) extends Watcher with Closeable {
  def this(connectString: String, sessionTimeout: Int) = this(connectString, sessionTimeout, false)

  private val zk = new ReconnectingZk(connectString, sessionTimeout, this, readOnly)
  private val properties = collection.concurrent.TrieMap[String, String]()

  def apply(key: String): Option[String] = properties.get(key)

  def process(event: WatchedEvent) {
    event.getType match {
      case EventType.None if event.getState == KeeperState.SyncConnected => fetchProperties(rewriteKnownProperties = true)
      case EventType.NodeChildrenChanged                                 => fetchProperties(rewriteKnownProperties = false)
      case EventType.NodeDataChanged                                     => fetchProperty()
    }

    def fetchProperties(rewriteKnownProperties: Boolean) {
      zk.get.getChildren("/", this, new ChildrenCallback(zk.get, this, properties, rewriteKnownProperties), null)
    }

    def fetchProperty() {
      zk.get.getData(event.getPath, this, new PropertyCallback(properties, event.getPath.replace("/", "")), null)
    }
  }

  def close() {
    zk.close()
  }

  private class PropertyCallback(properties: collection.mutable.Map[String, String], key: String)
    extends AsyncCallback.DataCallback {

    def processResult(rc: Int, path: String, ctx: Any, data: Array[Byte], stat: Stat) {
      properties.update(key, new String(data))
    }
  }

  private class ChildrenCallback(zk: => ZooKeeper,
                                 watcher: Watcher,
                                 properties: collection.mutable.Map[String, String],
                                 rewriteKnownProperties: Boolean)
    extends AsyncCallback.ChildrenCallback {

    def processResult(rc: Int, path: String, ctx: Any, children: java.util.List[String]) {
      val knownChildren = properties.keySet
      val freshChildren = children.to[Set]
      val deletedChildren = knownChildren &~ freshChildren
      deletedChildren.foreach(properties -= _)
      val propertiesToFetch = if (rewriteKnownProperties) freshChildren else freshChildren &~ knownChildren
      propertiesToFetch.foreach(c => zk.getData(path + c, watcher, new PropertyCallback(properties, c), null))
    }
  }
}
