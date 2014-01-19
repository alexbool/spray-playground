package com.alexb.join

import org.scalatest.{Matchers, WordSpec}
import Implicits._

class JoinSpec extends WordSpec with Matchers {
  "Join class" should {
    "do one-to-many inner joins" in {
      val left = List(1, 2, 3)
      val right = List(2, 2, 3)
      Join.join(left, right, identity[Int], identity[Int]) should be (List((2, Seq(2, 2)), (3, Seq(3))))
    }
    "do one-to-many inner joins with implicit conversion" in {
      List(1, 2, 3).join(List(2, 2, 3), identity[Int], identity[Int]) should be (List((2, Seq(2, 2)), (3, Seq(3))))
    }
    "do one-to-many left joins" in {
      val left = List(1, 2, 3)
      val right = List(2, 2, 3)
      Join.leftJoin(left, right, identity[Int], identity[Int]) should be (List((1, Seq()), (2, Seq(2, 2)), (3, Seq(3))))
    }
    "do one-to-many left joins with implicit conversion" in {
      List(1, 2, 3).leftJoin(List(2, 2, 3), identity[Int], identity[Int]) should be (List((1, Seq()), (2, Seq(2, 2)), (3, Seq(3))))
    }
    "do one-to-one inner joins" in {
      val left = List(1, 2, 3)
      val right = List(2, 3)
      Join.oneToOneJoin(left, right, identity[Int], identity[Int]) should be (List((2, 2), (3, 3)))
    }
    "do one-to-one inner joins with implicit conversion" in {
      List(1, 2, 3).oneToOneJoin(List(2, 3), identity[Int], identity[Int]) should be (List((2, 2), (3, 3)))
    }
    "do one-to-one left joins" in {
      val left = List(1, 2, 3)
      val right = List(2, 3)
      Join.oneToOneLeftJoin(left, right, identity[Int], identity[Int]) should be (List((1, None), (2, Some(2)), (3, Some(3))))
    }
    "do one-to-one left joins with implicit conversion" in {
      List(1, 2, 3).oneToOneLeftJoin(List(2, 3), identity[Int], identity[Int]) should be (List((1, None), (2, Some(2)), (3, Some(3))))
    }
  }
}
