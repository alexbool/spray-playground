package com.alexb.statics

import akka.actor.ActorSystem
import akka.dispatch.Future
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport
import com.alexb.memoize.{ Memoize, CacheManager }
import com.alexb.main.context.MongoSupport

trait StaticsService
  extends HttpService
  with SprayJsonSupport
  with StaticsMarshallers
  with Memoize {

  this: CountryRepository =>

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

trait StaticsModule extends StaticsService with MongoCountryRepository {
  this: MongoSupport =>
}
