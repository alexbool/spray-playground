package com.alexb.statics

import scala.concurrent.{ExecutionContext, Future}
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport
import com.alexb.memoize.Memoize
import com.alexb.main.context.{ActorSystemContext, Caching, MongoSupport}

trait StaticsService
  extends HttpService
  with SprayJsonSupport
  with StaticsMarshallers
  with Memoize { this: CountryRepository with Caching with ActorSystemContext =>

  implicit private val cache = cacheManager
  implicit private def ec: ExecutionContext = actorSystem.dispatcher

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

  private val countries = memoizeAsyncValue("statics", "countries", Future(findCountries))
}

trait StaticsModule extends StaticsService with MongoCountryRepository {
  this: MongoSupport with Caching with ActorSystemContext =>
}
