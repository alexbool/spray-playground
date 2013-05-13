package com.alexb.swift

import spray.client.pipelining._
import spray.http.{HttpResponse, HttpRequest}

trait Authentication {

  def authenticationRequest(credentials: Credentials, authUrl: String): HttpRequest =
    Get(authUrl) ~> addHeader("X-Auth-User", credentials.user) ~> addHeader("X-Auth-Key", credentials.key)

  def authResultFromResponse(response: HttpResponse, revision: Int) =
    AuthenticationResult(
      response.headers.find(_.is("x-auth-token")).map(_.value).get,
      response.headers.find(_.is("x-storage-url")).map(_.value).get,
      revision)
}
