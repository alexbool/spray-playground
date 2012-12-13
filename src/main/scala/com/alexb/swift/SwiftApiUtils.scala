package com.alexb.swift

import spray.client.HttpConduit._

private[swift] trait SwiftApiUtils {
  private val formatJson = "?format=json"

  def mkUrl(root: String, account: String)                                      = s"$root/$account"
  def mkUrl(root: String, account: String, container: String)                   = s"$root/$account/$container"
  def mkUrl(root: String, account: String, container: String, `object`: String) = s"$root/$account/$container/${`object`}"

  def mkUrlJson(root: String, account: String)                                      = s"${mkUrl(root, account)}$formatJson"
  def mkUrlJson(root: String, account: String, container: String)                   = s"${mkUrl(root, account, container)}$formatJson"
  def mkUrlJson(root: String, account: String, container: String, `object`: String) = s"${mkUrl(root, account, container, `object`)}$formatJson"

  def authHeader(token: String) = addHeader("X-Storage-Token", token)
}
