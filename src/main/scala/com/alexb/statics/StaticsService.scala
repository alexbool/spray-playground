package com.alexb.statics

import akka.actor.ActorSystem
import akka.dispatch.Future
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport
import com.alexb.memoize.Memoize
import com.alexb.main.context.{Caching, MongoSupport}

trait StaticsService
  extends HttpService
  with SprayJsonSupport
  with StaticsMarshallers
  with Memoize {

  this: CountryDao with Caching =>

  implicit def actorSystem: ActorSystem
  implicit val cache = cacheManager

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

trait StaticsModule extends StaticsService with MongoCountryDao {
  this: MongoSupport with Caching =>
}
