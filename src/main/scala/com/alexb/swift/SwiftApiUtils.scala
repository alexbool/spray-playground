package com.alexb.swift

import spray.client.HttpConduit._

private[swift] trait SwiftApiUtils {
  private val root = "/v1.0"

  def accountUrl(account: String)                                     = s"$root/$account"
  def containerUrl(account: String, container: String)                = s"$root/$account/$container"
  def objectUrl(account: String, container: String, `object`: String) = s"$root/$account/$container/$`object`"

  def authHeader(token: String) = addHeader("X-Storage-Token", token)
}
