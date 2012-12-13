package com.alexb.swift

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
