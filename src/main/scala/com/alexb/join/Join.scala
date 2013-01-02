package com.alexb.join

import scala.collection.mutable

object Join {
  def join[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightMultiMap = iterableToMultiMap(right.map(e => (rightKey(e), e)))
    for {
      leftElem <- left.map(e => (leftKey(e), e)) if rightMultiMap.contains(leftElem._1)
    } yield (leftElem._2, rightMultiMap.get(leftElem._1).get.to[Seq])
  }

  def innerJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) =
    join(left, right, leftKey, rightKey)

  def leftJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightMultiMap = iterableToMultiMap(right.map(e => (rightKey(e), e)))
    (for {
      leftElem <- left.map(e => (leftKey(e), e))
    } yield (leftElem._2, rightMultiMap.get(leftElem._1)))
    .map({ e =>
      e._2 match {
        case None => (e._1, Seq[B]())
        case Some(list) => (e._1, list.to[Seq])
      }
    })
  }

  def oneToOneJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightMap = right.map(e => (rightKey(e), e)).toMap
    for {
      leftElem <- left.map(e => (leftKey(e), e)) if rightMap.contains(leftElem._1)
    } yield (leftElem._2, rightMap.get(leftElem._1).get)
  }

  def oneToOneInnerJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) =
    join(left, right, leftKey, rightKey)

  def oneToOneLeftJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightMap = right.map(e => (rightKey(e), e)).toMap
    for {
      leftElem <- left.map(e => (leftKey(e), e))
    } yield (leftElem._2, rightMap.get(leftElem._1))
  }

  private def iterableToMultiMap[A, B](collection: Iterable[(A, B)]) =
    collection.foldLeft(new mutable.HashMap[A, mutable.ListBuffer[B]]) { (acc, pair) =>
      acc.get(pair._1) match {
        case None =>
          val list = mutable.ListBuffer[B]()
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
    def oneToOneJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = Join.oneToOneJoin(left, right, leftKey, rightKey)
    def oneToOneInnerJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = oneToOneJoin(right, leftKey, rightKey)
    def oneToOneLeftJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = Join.oneToOneLeftJoin(left, right, leftKey, rightKey)
  }
}
