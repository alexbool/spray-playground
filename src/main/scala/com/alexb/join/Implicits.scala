package com.alexb.join

object Implicits {
  implicit class Joinable[A](left: Iterable[A]) {
    def join[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = Join.join(left, right, leftKey, rightKey)
    def innerJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = join(right, leftKey, rightKey)
    def leftJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = Join.leftJoin(left, right, leftKey, rightKey)
    def oneToOneJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = Join.oneToOneJoin(left, right, leftKey, rightKey)
    def oneToOneInnerJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = oneToOneJoin(right, leftKey, rightKey)
    def oneToOneLeftJoin[B, K](right: Iterable[B], leftKey: A => K, rightKey: B => K) = Join.oneToOneLeftJoin(left, right, leftKey, rightKey)
  }
}
