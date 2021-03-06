package com.alexb.swift

import org.joda.time.Instant
import spray.http.MediaType

case class Container(name: String, count: Int, bytes: Long)
case class ObjectMetadata(name: String, hash: String, bytes: Long, contentType: String, lastModified: Instant)

case class Object(name: String, mediaType: MediaType, data: Array[Byte])

case class Credentials(user: String, key: String)

private[swift] case class AuthenticationResult(token: String, storageUrl: String, revision: Int)
