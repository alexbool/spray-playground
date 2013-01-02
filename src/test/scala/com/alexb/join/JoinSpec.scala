package com.alexb.join

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import Join._

class JoinSpec extends WordSpec with MustMatchers {
  "Join class" must {
    "do one-to-many inner joins" in {
      val left = List(1, 2, 3)
      val right = List(2, 2, 3)
      Join.join(left, right, identity[Int], identity[Int]) must be (Iterable((2, Seq(2, 2)), (3, Seq(3))))
    }
    "do one-to-many inner joins with implicit conversion" in {
      List(1, 2, 3).join(List(2, 2, 3), identity[Int], identity[Int]) must be (Iterable((2, Seq(2, 2)), (3, Seq(3))))
    }
    "do one-to-many left joins" in {
      val left = List(1, 2, 3)
      val right = List(2, 2, 3)
      Join.leftJoin(left, right, identity[Int], identity[Int]) must be (Iterable((1, Seq()), (2, Seq(2, 2)), (3, Seq(3))))
    }
    "do one-to-many left joins with implicit conversion" in {
      List(1, 2, 3).leftJoin(List(2, 2, 3), identity[Int], identity[Int]) must be (Iterable((1, Seq()), (2, Seq(2, 2)), (3, Seq(3))))
    }
  }
}
