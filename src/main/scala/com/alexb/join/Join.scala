package com.alexb.join

object Join {
  def join[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightGrouped = right.groupBy(rightKey(_))
    for {
      leftElem <- left.map(e => (leftKey(e), e)) if rightGrouped.contains(leftElem._1)
    } yield (leftElem._2, rightGrouped.get(leftElem._1).get.to[Seq])
  }

  def innerJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) =
    join(left, right, leftKey, rightKey)

  def leftJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightGrouped = right.groupBy(rightKey(_))
    (for {
      leftElem <- left.map(e => (leftKey(e), e))
    } yield (leftElem._2, rightGrouped.get(leftElem._1)))
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
}
