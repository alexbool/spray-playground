package com.alexb.utils

import shapeless._
import spray.routing.Directive
import spray.routing.directives.ParameterDirectives._

case class PageInfo(num: Int, size: Int) {
	require(num > 0, "Page number must be positive")
	require(size > 0, "Page size must be positive")
}

trait PageDirectives {
	
	def pageInfo: Directive[PageInfo :: HNil] =
			parameters('page.as[Int] ? 1, 'size.as[Int] ? 10).as(PageInfo)
}
