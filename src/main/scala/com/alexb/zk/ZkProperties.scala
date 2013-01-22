package com.alexb.zk

import org.apache.zookeeper.{AsyncCallback, WatchedEvent, Watcher, ZooKeeper}
import org.apache.zookeeper.Watcher.Event.{KeeperState, EventType}
import org.apache.zookeeper.data.Stat
import java.io.Closeable
import scala.collection.JavaConversions._

class ZkProperties(connectString: String, sessionTimeout: Int) extends Watcher with Closeable {
  private var zk = newZk
  private val properties = collection.concurrent.TrieMap[String, String]()

  def apply(key: String): Option[String] = properties.get(key)

  def process(event: WatchedEvent) {
    event.getType match {
      case EventType.None if event.getState == KeeperState.SyncConnected => fetchProperties()
      case EventType.None if event.getState == KeeperState.Expired       => refreshZk()
      case EventType.NodeChildrenChanged                                 => fetchProperties()
      case EventType.NodeDataChanged                                     => fetchProperty()
    }

    def fetchProperties() {
      zk.getChildren("/", this, new ChildrenCallback(zk, this, properties), null)
    }

    def fetchProperty() {
      zk.getData(event.getPath, this, new PropertyCallback(properties, event.getPath.replace("/", "")), null)
    }

    def refreshZk() {
      zk.close()
      zk = newZk
    }
  }

  def close() {
    zk.close()
  }

  private def newZk = new ZooKeeper(connectString, sessionTimeout, this, true)

  private class PropertyCallback(properties: collection.mutable.Map[String, String], key: String)
    extends AsyncCallback.DataCallback {

    def processResult(rc: Int, path: String, ctx: Any, data: Array[Byte], stat: Stat) {
      properties.update(key, new String(data))
    }
  }

  private class ChildrenCallback(zk: ZooKeeper, watcher: Watcher, properties: collection.mutable.Map[String, String])
    extends AsyncCallback.ChildrenCallback {

    def processResult(rc: Int, path: String, ctx: Any, children: java.util.List[String]) {
      val knownChildren = properties.keySet
      val freshChildren = children.to[Set]
      val deletedChildren = knownChildren &~ freshChildren
      deletedChildren.foreach(properties -= _)
      val createdChildren = freshChildren &~ knownChildren
      createdChildren.foreach(c => zk.getData(path + c, watcher, new PropertyCallback(properties, c), null))
    }
  }
}
