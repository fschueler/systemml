package org.apache.sysml.api.linalg

import org.apache.spark.sql.DataFrame
import org.apache.sysml.api.linalg.Lazy.Tree
import org.apache.sysml.api.linalg.api.:::
import org.apache.sysml.api.linalg.types.data.DataContainer

import scala.collection.immutable.Range.Inclusive

class LazyVector(override val tree: Tree) extends Matrix[LazyVector, LazyMatrix] with Lazy {
  override def rows = 0

  override def cols = 0

  override def impl: DataContainer[_] = null

  /**
    * Reshapes the [[Vector]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows  number of rows of the new T
    * @param cols  number of columns of the new T
    * @param byRow if true, T is reshaped my row
    * @return new T with the new dimensions and rearranged values
    */
  override def reshape(rows: Int, cols: Int, byRow: Boolean): LazyMatrix = ???

  override def copy: LazyVector = ???

  override def toDataFrame: DataFrame = ???

  override def apply(row: Int, col: Int): Double = ???

  override def apply(row: Int, col: :::.type): LazyVector = ???

  override def apply(row: :::.type, col: Int): LazyVector = ???

  override def apply(rows: Inclusive, cols: :::.type): LazyMatrix = ???

  override def apply(rows: :::.type, cols: Inclusive): LazyMatrix = ???

  override def apply(rows: Inclusive, cols: Inclusive): LazyMatrix = ???

  override def update(row: Int, col: Int, value: Double): LazyMatrix = ???

  override def update(row: Int, col: :::.type, vec: LazyVector): LazyMatrix = ???

  override def update(row: :::.type, col: Int, vec: LazyVector): LazyMatrix = ???

  override def update(rows: Inclusive, cols: :::.type, mat: LazyMatrix): LazyMatrix = ???

  override def update(rows: :::.type, cols: Inclusive, mat: LazyMatrix): LazyMatrix = ???

  override def update(rows: Inclusive, cols: Inclusive, mat: LazyMatrix): LazyMatrix = ???

  override def +(that: Double): LazyVector = ???

  override def -(that: Double): LazyMatrix = ???

  override def *(that: Double): LazyMatrix = ???

  override def /(that: Double): LazyMatrix = ???

  override def +(that: LazyVector): LazyMatrix = ???

  override def -(that: LazyVector): LazyMatrix = ???

  override def *(that: LazyVector): LazyMatrix = ???

  override def /(that: LazyVector): LazyMatrix = ???

  override def +(that: LazyMatrix): LazyMatrix = ???

  override def -(that: LazyMatrix): LazyMatrix = ???

  override def *(that: LazyMatrix): LazyMatrix = ???

  override def /(that: LazyMatrix): LazyMatrix = ???

  override def %*%(that: LazyMatrix): LazyMatrix = ???

  override def %*%(that: LazyVector): LazyVector = ???

  override def t: LazyVector = ???

  override def ^(n: Int): LazyVector = ???

  override def map(f: (Double) => Double): LazyVector = ???
}
