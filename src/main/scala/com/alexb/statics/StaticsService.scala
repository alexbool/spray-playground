package com.alexb.statics

import scala.concurrent.{ExecutionContext, Future}
import spray.routing.Directives
import spray.httpx.SprayJsonSupport
import com.alexb.memoize.CacheManager
import com.alexb.memoize.Implicits._
import com.alexb.main.context.{ActorSystemContext, Caching, MongoSupport}
import com.alexb.main.HttpRouteContainer

class StaticsService(countryRepository: CountryRepository)(implicit ec: ExecutionContext, cache: CacheManager)
  extends Directives with SprayJsonSupport with StaticsMarshallers with HttpRouteContainer {

  private val countries = Future(countryRepository.findCountries).memoizedAsync("statics", "countries")

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
}

trait StaticsServiceContext {
  this: MongoSupport with Caching with ActorSystemContext =>

  private lazy val countryRepository = new MongoCountryRepository(mongoDb)
  lazy val staticsService = new StaticsService(countryRepository)(actorSystem.dispatcher, cacheManager)
}
