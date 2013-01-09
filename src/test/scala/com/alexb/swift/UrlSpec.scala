package com.alexb.swift

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import java.lang.IllegalArgumentException

class UrlSpec extends WordSpec with MustMatchers {
  "Url class" must {
    "parse url" in {
      Url("http://google.com") must equal(Url("google.com", 80, false, "/"))
      Url("https://google.com") must equal(Url("google.com", 443, true, "/"))
      Url("http://google.com:8080") must equal(Url("google.com", 8080, false, "/"))
      Url("http://google.com/path/another-path") must equal(Url("google.com", 80, false, "/path/another-path"))
      Url("http://google.com:8080/path/another-path") must equal(Url("google.com", 8080, false, "/path/another-path"))
      Url("https://google.com:8080/path/another-path") must equal(Url("google.com", 8080, true, "/path/another-path"))
    }
    "not parse malformed urls" in {
      evaluating { Url("ololo") } must produce [IllegalArgumentException]
    }
    "serialize urls to string" in {
      Url("google.com", 80, false, "/").toString must be ("http://google.com/")
      Url("google.com", 8080, false, "/").toString must be ("http://google.com:8080/")
      Url("google.com", 443, true, "/").toString must be ("https://google.com/")
      Url("google.com", 8080, true, "/").toString must be ("https://google.com:8080/")
    }
  }
}
