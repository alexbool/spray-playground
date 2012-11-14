package com.alexb.statics

import akka.dispatch.Future
import com.mongodb.casbah.Imports._
import com.alexb.main.context.MongoSupport

trait CountryRepository {
  def findCountries: Seq[Country]
}

trait MongoCountryRepository extends CountryRepository {
  this: MongoSupport =>

  val countryCollection = mongoDb("countries")

  def findCountries =
    countryCollection
      .map(d => Country(d.getAs[Number]("_id").get.intValue, d.getAs[String]("name").get,
        d.getAs[Seq[DBObject]]("cities").get.map(c => City(c.getAs[Number]("id").get.intValue, c.getAs[String]("name").get)))
      )
      .toSeq
}
