package org.apache.sysml.api.linalg

import org.apache.spark.sql.DataFrame
import org.apache.sysml.api.linalg.Lazy.Tree
import org.apache.sysml.api.linalg.api.:::

import scala.collection.immutable.Range.Inclusive

class LazyVector(override val tree: Tree) extends Matrix[LazyVector] with Lazy {
  override val cols = 1
  override val rows: Int = _

  override def apply(row: Int, col: Int): Double = ???

  override def apply(row: Int, col: :::.type): Vector = ???

  override def apply(row: :::.type, col: Int): Vector = ???

  override def apply(rows: Inclusive, cols: :::.type): LazyVector = ???

  override def apply(rows: :::.type, cols: Inclusive): LazyVector = ???

  override def apply(rows: Inclusive, cols: Inclusive): LazyVector = ???

  // TODO make sure that the orientation of the vector (row/col) fits the assignment
  override def update(row: Int, col: Int, value: Double): LazyVector = ???

  override def update(row: Int, col: :::.type, vec: Vector): LazyVector = ???

  override def update(row: :::.type, col: Int, vec: Vector): LazyVector = ???

  override def update(rows: Inclusive, cols: :::.type, mat: LazyVector): LazyVector = ???

  override def update(rows: :::.type, cols: Inclusive, mat: LazyVector): LazyVector = ???

  override def update(rows: Inclusive, cols: Inclusive, mat: LazyVector): LazyVector = ???

  override def +(that: Double): LazyVector = ???

  override def -(that: Double): LazyVector = ???

  override def *(that: Double): LazyVector = ???

  override def /(that: Double): LazyVector = ???

  override def +(that: Vector): LazyVector = ???

  override def -(that: Vector): LazyVector = ???

  override def *(that: Vector): LazyVector = ???

  override def /(that: Vector): LazyVector = ???

  override def +(that: LazyVector): LazyVector = ???

  override def -(that: LazyVector): LazyVector = ???

  override def *(that: LazyVector): LazyVector = ???

  override def /(that: LazyVector): LazyVector = ???

  override def %*%(that: LazyVector): LazyVector = ???

  override def %*%(that: Vector): Vector = ???

  override def t: LazyVector = ???

  override def ^(n: Int): LazyVector = ???

  override def map(f: (Double) => Double): LazyVector = ???

  /**
    * Reshapes the [[T]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows  number of rows of the new T
    * @param cols  number of columns of the new T
    * @param byRow if true, T is reshaped my row
    * @return new T with the new dimensions and rearranged values
    */
  override def reshape(rows: Int, cols: Int, byRow: Boolean): LazyVector = ???

  override def copy: LazyVector = ???

  override def toDataFrame: DataFrame = ???
}
