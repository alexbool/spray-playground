package com.alexb.statics

import com.mongodb.casbah.Imports._

trait CountryRepository {
  def findCountries: Seq[Country]
}

class MongoCountryRepository(mongoDb: MongoDB) extends CountryRepository {

  private val countryCollection = mongoDb("countries")

  def findCountries =
    countryCollection
      .map(d => Country(d.getAs[Number]("_id").get.intValue, d.getAs[String]("name").get,
        d.getAs[Seq[DBObject]]("cities").get.map(c => City(c.getAs[Number]("id").get.intValue, c.getAs[String]("name").get)))
      )
      .to[Seq]
}
