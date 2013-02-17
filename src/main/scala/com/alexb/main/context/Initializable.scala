package com.alexb.main.context

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
