package com.alexb.swift

import spray.client.pipelining._

private[swift] trait SwiftApiUtils {
  private val formatJson = "?format=json"

  def mkUrl(root: String, container: String)                   = s"$root/$container"
  def mkUrl(root: String, container: String, `object`: String) = s"$root/$container/${`object`}"

  def mkUrlJson(root: String)                                      = s"${root}$formatJson"
  def mkUrlJson(root: String, container: String)                   = s"${mkUrl(root, container)}$formatJson"
  def mkUrlJson(root: String, container: String, `object`: String) = s"${mkUrl(root, container, `object`)}$formatJson"

  def authHeader(token: String) = addHeader("X-Storage-Token", token)
}
