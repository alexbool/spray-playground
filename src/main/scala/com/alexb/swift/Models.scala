package com.alexb.swift

import org.joda.time.Instant

case class Container(name: String, count: Int, bytes: Long)
case class ObjectMetadata(name: String, hash: String, bytes: Long, contentType: String, lastModified: Instant)
