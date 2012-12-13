package com.alexb.swift

import spray.client.HttpConduit._

private[swift] trait SwiftApiUtils {
  def accountUrl(root: String, account: String)                                     = s"$root/$account"
  def containerUrl(root: String, account: String, container: String)                = s"$root/$account/$container"
  def objectUrl(root: String, account: String, container: String, `object`: String) = s"$root/$account/$container/${`object`}"

  def authHeader(token: String) = addHeader("X-Storage-Token", token)
}
