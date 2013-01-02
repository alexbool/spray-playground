package com.alexb.join

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Join {
  def join[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightMultiMap = iterableToMultiMap(right.map(e => (rightKey(e), e)))
    (for {
      leftElem <- left.map(e => (leftKey(e), e)) if rightMultiMap.contains(leftElem._1)
    } yield (leftElem._2, rightMultiMap.get(leftElem._1).get))
    .flatMap({ e => e._2.map(s => (e._1, s)) })
  }

  def innerJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) =
    join(left, right, leftKey, rightKey)

  def leftJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightMultiMap = iterableToMultiMap(right.map(e => (rightKey(e), e)))
    (for {
      leftElem <- left.map(e => (leftKey(e), e))
    } yield (leftElem._2, rightMultiMap.get(leftElem._1)))
    .flatMap({ e =>
      e._2 match {
        case None => List((e._1, None))
        case Some(list) => list.map(s => (e._1, Some(s)))
      }
    })
  }

  private def iterableToMultiMap[A, B](collection: Iterable[(A, B)]) =
    collection.foldLeft(new mutable.HashMap[A, mutable.ListBuffer[B]]) { (acc, pair) =>
      acc.get(pair._1) match {
        case None =>
          val list = ListBuffer[B]()
          list += pair._2
          acc.put(pair._1, list)
        case Some(list) =>
          list += pair._2
      }
      acc
    }

  implicit class Joinable[A](left: Iterable[A]) {
    def join[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = Join.join(left, right, leftKey, rightKey)
    def innerJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = join(right, leftKey, rightKey)
    def leftJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = Join.leftJoin(left, right, leftKey, rightKey)
  }
}
