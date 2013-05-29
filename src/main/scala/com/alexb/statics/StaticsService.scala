package com.alexb.statics

import scala.concurrent.{ExecutionContext, Future}
import spray.routing.Directives
import spray.httpx.SprayJsonSupport
import com.alexb.memoize.{CacheManager, Memoize}
import com.alexb.main.context.{ActorSystemContext, Caching, MongoSupport}
import com.alexb.main.HttpRouteContainer

class StaticsService(countryRepository: CountryRepository)(implicit ec: ExecutionContext, cache: CacheManager)
  extends Directives with SprayJsonSupport with StaticsMarshallers with Memoize with HttpRouteContainer {

  val route =
    pathPrefix("static") {
      get {
        path("countries") {
          complete {
            countries()
          }
        }
      }
    }

  private val countries = memoizeAsyncValue("statics", "countries", Future(countryRepository.findCountries))
}

trait StaticsServiceContext {
  this: MongoSupport with Caching with ActorSystemContext =>

  private lazy val countryRepository = new MongoCountryRepository(mongoDb)
  lazy val staticsService = new StaticsService(countryRepository)(actorSystem.dispatcher, cacheManager)
}
