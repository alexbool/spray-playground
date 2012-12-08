package com.alexb.statics

import spray.routing.HttpService
import spray.httpx.SprayJsonSupport
import com.alexb.memoize.Memoize
import com.alexb.main.context.{ActorSystemContext, Caching, MongoSupport}

trait StaticsService
  extends HttpService
  with SprayJsonSupport
  with StaticsMarshallers
  with Memoize {

  this: CountryRepository with Caching with ActorSystemContext =>

  implicit private val cache = cacheManager

  val staticsRoute =
    pathPrefix("static") {
      get {
        path("countries") {
          complete {
            countries()
          }
        }
      }
    }

  private val countries = memoizeAsync("statics", "countries", () => findCountries)
}

trait StaticsModule extends StaticsService with MongoCountryRepository {
  this: MongoSupport with Caching with ActorSystemContext =>
}
