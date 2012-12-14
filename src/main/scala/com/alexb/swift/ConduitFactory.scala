package com.alexb.swift

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import scala.concurrent.duration._
import spray.client.HttpConduit

case class HttpConduitId(host: String, port: Int, sslEnabled: Boolean)

class ConduitFactory(httpClient: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher

  private val conduitHolder = collection.mutable.Map.empty[HttpConduitId, ActorRef]

  def receive = {
    case msg: HttpConduitId => sender ! conduitFor(msg)
  }

  def conduitFor(id: HttpConduitId): ActorRef =
    conduitHolder.get(id) match {
      case Some(conduit) => conduit
      case None =>
        log.debug(s"Cannot find existing HttpConduit for host=${id.host}, port=${id.port}, sslEnabled=${id.sslEnabled}" +
          ", creating new")
        val conduit = context.actorOf(
          props = Props(new HttpConduit(httpClient, id.host, id.port, id.sslEnabled)),
          name = s"http-conduit-${id.host}-${id.port}-${ if (id.sslEnabled) "ssl" else "nossl"}")
        conduitHolder.values.foreach { conduit =>
          // Shutting down existing conduits after a reasonable timeout
          log.debug(s"Scheduling existing HttpConduit ${conduit.toString()} shutdown")
          context.system.scheduler.scheduleOnce(1 minute)({
            log.debug(s"Shutting down HttpConduit ${conduit.toString()}")
            context.stop(conduit)
          })
        }
        conduitHolder.clear()
        conduitHolder.put(id, conduit)
        conduit
      }
}
