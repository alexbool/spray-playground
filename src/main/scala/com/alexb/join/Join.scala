package com.alexb.join

/**
 * This object contains several methods for SQL-like joining collections.
 *
 * @author Alexander Bulaev
 */
object Join {

  /**
   * Joins elements of right collection to the left collection, following the rule:
   * `leftKey(leftElem) == rightKey(rightElem)`.
   *
   * Elements from left collection with no matches in right collection are skipped.
   *
   * @param left     left collection
   * @param right    right collection
   * @param leftKey  left collection key projection
   * @param rightKey right collection key projection
   * @tparam A       left collection type
   * @tparam B       right collection type
   * @tparam K       key type
   * @return         joined collection
   */
  def join[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightGrouped = right.groupBy(rightKey(_))
    for {
      leftElem <- left.map(e => (leftKey(e), e)) if rightGrouped.contains(leftElem._1)
    } yield (leftElem._2, rightGrouped.get(leftElem._1).get.to[Seq])
  }

  /**
   * Alias for [[com.alexb.join.Join.join]]
   *
   * @param left     left collection
   * @param right    right collection
   * @param leftKey  left collection key projection
   * @param rightKey right collection key projection
   * @tparam A       left collection type
   * @tparam B       right collection type
   * @tparam K       key type
   * @return         joined collection
   */
  def innerJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) =
    join(left, right, leftKey, rightKey)

  /**
   * Joins elements of right collection to the left collection, following the rule:
   * `leftKey(leftElem) == rightKey(rightElem)`.
   *
   * No elements from left collection are skipped, even if they have no matches in the right one.
   *
   * @param left     left collection
   * @param right    right collection
   * @param leftKey  left collection key projection
   * @param rightKey right collection key projection
   * @tparam A       left collection type
   * @tparam B       right collection type
   * @tparam K       key type
   * @return         joined collection
   */
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

  /**
   * Joins elements of right collection to the left collection, following the rule:
   * `leftKey(leftElem) == rightKey(rightElem)`.
   *
   * If two or more elements from the right collection are matched against an element from the left collection, only the
   * first one is taken.
   *
   * Elements from left collection with no matches in right collection are skipped.
   *
   * @param left     left collection
   * @param right    right collection
   * @param leftKey  left collection key projection
   * @param rightKey right collection key projection
   * @tparam A       left collection type
   * @tparam B       right collection type
   * @tparam K       key type
   * @return         joined collection
   */
  def oneToOneJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightMap = right.map(e => (rightKey(e), e)).toMap
    for {
      leftElem <- left.map(e => (leftKey(e), e)) if rightMap.contains(leftElem._1)
    } yield (leftElem._2, rightMap.get(leftElem._1).get)
  }

  /**
   * Alias for [[com.alexb.join.Join.oneToOneJoin]]
   *
   * @param left     left collection
   * @param right    right collection
   * @param leftKey  left collection key projection
   * @param rightKey right collection key projection
   * @tparam A       left collection type
   * @tparam B       right collection type
   * @tparam K       key type
   * @return         joined collection
   */
  def oneToOneInnerJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) =
    oneToOneJoin(left, right, leftKey, rightKey)

  /**
   * Joins elements of right collection to the left collection, following the rule:
   * `leftKey(leftElem) == rightKey(rightElem)`.
   *
   * If two or more elements from the right collection are matched against an element from the left collection, only the
   * first one is taken.
   *
   * No elements from left collection are skipped, even if they have no matches in the right one.
   *
   * @param left     left collection
   * @param right    right collection
   * @param leftKey  left collection key projection
   * @param rightKey right collection key projection
   * @tparam A       left collection type
   * @tparam B       right collection type
   * @tparam K       key type
   * @return         joined collection
   */
  def oneToOneLeftJoin[A, B, K](left: Iterable[A], right: Iterable[B], leftKey: A => K, rightKey: B => K) = {
    val rightMap = right.map(e => (rightKey(e), e)).toMap
    for {
      leftElem <- left.map(e => (leftKey(e), e))
    } yield (leftElem._2, rightMap.get(leftElem._1))
  }
}
