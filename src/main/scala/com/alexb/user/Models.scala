package com.alexb.user

import org.joda.time.Instant

case class User(username: String, password: String, created: Instant)
