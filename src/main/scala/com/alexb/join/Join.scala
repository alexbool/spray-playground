package com.alexb.join

object Join {
  def join[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightMap = right.map(e => (rightKey(e), e)).toMap
    for {
      leftElem <- left.map(e => (leftKey(e), e)) if rightMap.contains(leftElem._1)
    } yield (leftElem._2, rightMap.get(leftElem._1).get)
  }

  def innerJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) =
    join(left, right, leftKey, rightKey)

  def leftJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightMap = right.map(e => (rightKey(e), e)).toMap
    for {
      leftElem <- left.map(e => (leftKey(e), e))
    } yield (leftElem._2, rightMap.get(leftElem._1))
  }

  implicit class Joinable[A](left: Iterable[A]) {
    def join[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = Join.join(left, right, leftKey, rightKey)
    def innerJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = join(right, leftKey, rightKey)
    def leftJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = Join.leftJoin(left, right, leftKey, rightKey)
  }
}
