package com.alexb.swift

import spray.client.HttpConduit._

private[swift] trait SwiftApiUtils {
  private val formatJson = "?format=json"

  def mkUrl(root: String, account: String)                                      = s"$root/$account"
  def mkUrl(root: String, account: String, container: String)                   = s"$root/$account/$container"
  def mkUrl(root: String, account: String, container: String, `object`: String) = s"$root/$account/$container/${`object`}"

  def authHeader(token: String) = addHeader("X-Storage-Token", token)
}
