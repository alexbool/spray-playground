package com.alexb.main.context

import scala.collection.mutable.ListBuffer

/**
 * A trait that delays all initialization of target object until the `initilaize` method is called.
 */
trait Initializable extends DelayedInit {
  private var isInitialized = false
  private val initBody = new ListBuffer[() => Unit]

  def delayedInit(body: => Unit) {
    initBody += (() => body)
  }

  def initialize() {
    this.synchronized {
      if (!isInitialized) {
        isInitialized = true
        for (initCode <- initBody) initCode()
      }
    }
  }
}
