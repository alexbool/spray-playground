package com.alexb.swift

import spray.client.HttpConduit._

private[swift] trait SwiftApiUtils {
  private val root = "/v1.0"

  def accountUrl(account: String)                                     = s"$root/$account"
  def containerUrl(account: String, container: String)                = s"$root/$account/$container"
  def objectUrl(account: String, container: String, `object`: String) = s"$root/$account/$container/${`object`}"

  def authHeader(token: String) = addHeader("X-Storage-Token", token)
}

case class Url(host: String, port: Int, sslEnabled: Boolean, path: String)

object Url {
  private val pattern = """(http(s)?)://([\w\.]+)(:(\d+))?(/[\w/-]*)?""".r

  def apply(url: String): Url = {
    val m = pattern.findFirstMatchIn(url)
    require(m.isDefined, "Cannot match url in input string")
    val host = m.get.group(3)
    val scheme = m.get.group(1)
    val port = Option(m.get.group(5)).map(_.toInt)
    val path = Option(m.get.group(6))
    val sslEnabled = scheme == "https"
    Url(
      host,
      port.getOrElse(if (sslEnabled) 443 else 80),
      sslEnabled,
      path.getOrElse("/")
    )
  }
}
