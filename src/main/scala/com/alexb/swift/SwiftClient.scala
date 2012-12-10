package com.alexb.swift

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.client.HttpConduit
import HttpConduit._

case class SwiftCredentials(user: String, key: String)

class SwiftClient(authUrl: String, credentials: SwiftCredentials, httpClient: ActorRef) extends Actor with ActorLogging {
  private val authPipeline =
    addHeader("X-Auth-User", credentials.user) ~>
    addHeader("X-Auth-Key", credentials.key) ~>
    sendReceive(authConduit)
  private val authConduit = context.system.actorOf(Props(new HttpConduit(httpClient, authUrl, 80)))

  private var authToken: Option[String] = None
  private var storageUrl: Option[String] = None
  private def getToken = authToken match {
    case Some(token) => token
    case None => {
      authenticate
      authToken.get
    }
  }

  private def authenticate {
    val f = Get("/v1.0") ~> authPipeline
    val res = Await.result(f, 10 seconds)
  }

  def receive = ???
}
