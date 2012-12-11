package com.alexb.swift

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.client.HttpConduit
import HttpConduit._

case class SwiftCredentials(user: String, key: String)

class SwiftClient(authUrl: String, credentials: SwiftCredentials, httpClient: ActorRef) extends Actor with ActorLogging {
  private val authConduit = context.system.actorOf(Props(new HttpConduit(httpClient, authUrl, 80)))

  private val authPipeline =
    addHeader("X-Auth-User", credentials.user) ~>
    addHeader("X-Auth-Key", credentials.key) ~>
    sendReceive(authConduit)

  private var isAuthenticated = false
  private var authToken: Option[String] = None
  private var storageUrl: Option[String] = None

  private def getToken = {
    if (!isAuthenticated) authenticate()
    authToken.get
  }

  private def authenticate() {
    val res = Await.result(Get("/v1.0") ~> authPipeline, 10 seconds)
    var success = res.status.isSuccess
    if (success) {
      authToken = res.headers.find(_.name == "X-Auth-Token").map(_.value)
      storageUrl = res.headers.find(_.name == "X-Storage-Url").map(_.value)
      if (authToken.isEmpty || storageUrl.isEmpty) success = false
    }
    isAuthenticated = success
  }

  def receive = ???
}
