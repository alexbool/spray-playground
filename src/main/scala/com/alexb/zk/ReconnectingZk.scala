package com.alexb.zk

import org.apache.zookeeper.{ZooKeeper, WatchedEvent, Watcher}
import org.apache.zookeeper.Watcher.Event.{KeeperState, EventType}
import java.io.Closeable

class ReconnectingZk(connectString: String, sessionTimeout: Int, watcher: Watcher, canBeReadOnly: Boolean)
  extends Closeable {

  @volatile
  private var zk = newZk

  def get = zk

  def close() {
    zk.close()
  }

  private def refreshZk() {
    zk.close()
    zk = newZk
  }

  private def newZk =
    new ZooKeeper(connectString, sessionTimeout, new DelegatingWatcher(Option(watcher), this), canBeReadOnly)

  private class DelegatingWatcher(delegate: Option[Watcher], parent: ReconnectingZk) extends Watcher {
    def process(event: WatchedEvent) {
      event.getType match {
        case EventType.None if event.getState == KeeperState.Expired => parent.refreshZk()
        case _                                                       => delegate.foreach(_.process(event))
      }
    }
  }
}
