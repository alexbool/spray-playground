package com.alexb.statics

case class Country(id: Int, name: String, cities: Seq[City])
case class City(id: Int, name: String)
