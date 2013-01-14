package com.alexb.join

import language.higherKinds

object Implicits {

  /**
   * This class contains several methods for SQL-like joining collections.
   *
   * @see [[com.alexb.join.Join]]
   * @author Alexander Bulaev
   */
  implicit class Joinable[A, Col[X] <: Iterable[X]](left: Col[A]) {
    import Join.CBF

    def join[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K)
                  (implicit cbf: CBF[Col, (A, Seq[B])]): Col[(A, Seq[B])] =
      Join.join(left, right, leftKey, rightKey)

    def innerJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K)
                       (implicit cbf: CBF[Col, (A, Seq[B])]): Col[(A, Seq[B])] =
      join(right, leftKey, rightKey)

    def leftJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K)
                      (implicit cbf: CBF[Col, (A, Seq[B])]): Col[(A, Seq[B])] =
      Join.leftJoin(left, right, leftKey, rightKey)

    def oneToOneJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K)
                          (implicit cbf: CBF[Col, (A, B)]): Col[(A, B)] =
      Join.oneToOneJoin(left, right, leftKey, rightKey)

    def oneToOneInnerJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K)
                               (implicit cbf: CBF[Col, (A, B)]): Col[(A, B)] =
      oneToOneJoin(right, leftKey, rightKey)

    def oneToOneLeftJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K)
                              (implicit cbf: CBF[Col, (A, Option[B])]): Col[(A, Option[B])] =
      Join.oneToOneLeftJoin(left, right, leftKey, rightKey)
  }
}
