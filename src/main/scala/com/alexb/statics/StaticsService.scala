package com.alexb.statics

import akka.actor.ActorSystem
import akka.dispatch.Future
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport
import com.alexb.memoize.{ Memoize, CacheManager }

trait StaticsService
  extends HttpService
  with SprayJsonSupport
  with StaticsMarshallers
  with Memoize {

  this: CountryDao =>

  implicit def actorSystem: ActorSystem
  implicit def cacheManager: CacheManager

  val staticsRoute =
    pathPrefix("static") {
      get {
        path("countries") {
          dynamic {
            complete {
              Future { countries }
            }
          }
        }
      }
    }

  def countries = memoize("statics", "countries", findCountries)
}

trait StaticsModule extends StaticsService with MongoCountryDao
