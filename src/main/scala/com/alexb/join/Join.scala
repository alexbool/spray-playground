package com.alexb.join

import scala.collection.generic.CanBuildFrom
import language.higherKinds

/**
 * This object contains several methods for SQL-like joining collections.
 *
 * @author Alexander Bulaev
 */
object Join {

  type CBF[Col[_], Elem] = CanBuildFrom[Col[Elem], Elem, Col[Elem]]

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
   * @tparam Col     left collection type
   * @return         joined collection, same type as the left collection
   */
  def join[A, B, K, Col[X] <: Iterable[X]](left: Col[A], right: Iterable[B], leftKey: A => K, rightKey: B => K)
                                          (implicit cbf: CBF[Col, (A, Seq[B])]): Col[(A, Seq[B])] = {
    val rightGrouped = right.groupBy(rightKey(_))
    val builder = cbf()
    for (leftElem <- left.map(e => (leftKey(e), e)) if rightGrouped.contains(leftElem._1))
    builder += ((leftElem._2, rightGrouped.get(leftElem._1).get.to[Seq]))
    builder.result()
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
   * @tparam Col     left collection type
   * @return         joined collection, same type as the left collection
   */
  def innerJoin[A, B, K, Col[X] <: Iterable[X]](left: Col[A], right: Iterable[B], leftKey: A => K, rightKey: B => K)
                                               (implicit cbf: CBF[Col, (A, Seq[B])]): Col[(A, Seq[B])] =
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
   * @tparam Col     left collection type
   * @return         joined collection, same type as the left collection
   */
  def leftJoin[A, B, K, Col[X] <: Iterable[X]](left: Col[A], right: Iterable[B], leftKey: A => K, rightKey: B => K)
                                              (implicit cbf: CBF[Col, (A, Seq[B])]): Col[(A, Seq[B])]= {
    val rightGrouped = right.groupBy(rightKey(_))
    val builder = cbf()
    for (leftElem <- left.map(e => (leftKey(e), e))) {
      val elem = (leftElem._2, rightGrouped.get(leftElem._1))
      builder += (elem._2 match {
        case None => (elem._1, Seq[B]())
        case Some(list) => (elem._1, list.to[Seq])
      })
    }
    builder.result()
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
   * @tparam Col     left collection type
   * @return         joined collection, same type as the left collection
   */
  def oneToOneJoin[A, B, K, Col[X] <: Iterable[X]](left: Col[A], right: Iterable[B], leftKey: A => K, rightKey: B => K)
                                                  (implicit cbf: CBF[Col, (A, B)]): Col[(A, B)] = {
    val rightMap = right.map(e => (rightKey(e), e)).toMap
    val builder = cbf()
    for (leftElem <- left.map(e => (leftKey(e), e)) if rightMap.contains(leftElem._1))
      builder += ((leftElem._2, rightMap.get(leftElem._1).get))
    builder.result()
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
   * @tparam Col     left collection type
   * @return         joined collection, same type as the left collection
   */
  def oneToOneInnerJoin[A, B, K, Col[X] <: Iterable[X]](left: Col[A], right: Iterable[B], leftKey: A => K, rightKey: B => K)
                                                       (implicit cbf: CBF[Col, (A, B)]): Col[(A, B)] =
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
   * @tparam Col     left collection type
   * @return         joined collection, same type as the left collection
   */
  def oneToOneLeftJoin[A, B, K, Col[X] <: Iterable[X]](left: Col[A], right: Iterable[B], leftKey: A => K, rightKey: B => K)
                                                      (implicit cbf: CBF[Col, (A, Option[B])]): Col[(A, Option[B])] = {
    val rightMap = right.map(e => (rightKey(e), e)).toMap
    val builder = cbf()
    for (leftElem <- left.map(e => (leftKey(e), e)))
      builder += ((leftElem._2, rightMap.get(leftElem._1)))
    builder.result()
  }
}
