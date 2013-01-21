package com.alexb.zk

import org.apache.zookeeper.{AsyncCallback, WatchedEvent, Watcher, ZooKeeper}
import org.apache.zookeeper.Watcher.Event.{KeeperState, EventType}
import org.apache.zookeeper.data.Stat
import java.io.Closeable
import scala.collection.JavaConversions._

class ZkProperties(connectString: String, sessionTimeout: Int) extends Watcher with Closeable {
  private val zk = new ZooKeeper(connectString, sessionTimeout, this, true)
  private val properties = collection.concurrent.TrieMap[String, String]()

  def apply(key: String): Option[String] = properties.get(key)

  def process(event: WatchedEvent) {
    event.getType match {
      case EventType.None if event.getState == KeeperState.SyncConnected =>
        fetchProperties()
      case EventType.NodeChildrenChanged =>
        fetchProperties()
      case EventType.NodeDataChanged =>
        zk.getData(event.getPath, this, new PropertyCallback(properties, toKey(event.getPath)), null)
      case EventType.NodeDeleted =>
        properties -= toKey(event.getPath)
    }

    def fetchProperties() {
      zk.getChildren("/", this, new ChildrenCallback(zk, this, properties), null)
    }

    def toKey(path: String) = path.replace("/", "")
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

  private class ChildrenCallback(zk: ZooKeeper, watcher: Watcher, properties: collection.mutable.Map[String, String])
    extends AsyncCallback.ChildrenCallback {

    def processResult(rc: Int, path: String, ctx: Any, children: java.util.List[String]) {
      children.foreach(c => zk.getData(path + c, watcher, new PropertyCallback(properties, c), null))
    }
  }
}
