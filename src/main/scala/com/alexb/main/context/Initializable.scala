package com.alexb.main.context

/**
 * A trait that delays all initialization of target object until the `initilaize` method is called.
 */
trait Initializable extends DelayedInit {
  private var isInitialized = false
  private var initBody: () => Unit = null

  def delayedInit(body: => Unit) {
    initBody = () => body
  }

  def initialize() {
    this.synchronized {
      if (!isInitialized) {
        isInitialized = true
        initBody()
      }
    }
  }
}
