package org.apache.sysml.api.linalg
import org.apache.spark.sql.DataFrame
import org.apache.sysml.api.linalg.Lazy.{BinOp, Scalar, Tree}
import org.apache.sysml.api.linalg.api.:::
import org.apache.sysml.api.linalg.types.data.DataContainer

import scala.collection.immutable.Range.Inclusive

private class LazyMatrix(override val tree: Tree) extends Matrix[LazyVector, LazyMatrix] with Lazy {

  override def impl: DataContainer[_] = null

  override def apply(row: Int, col: Int): Double = ???

  override def apply(row: Int, col: :::.type): Vector = ???

  override def apply(row: :::.type, col: Int): Vector = ???

  override def apply(rows: Inclusive, cols: :::.type): LazyMatrix = ???

  override def apply(rows: :::.type, cols: Inclusive): LazyMatrix = ???

  override def apply(rows: Inclusive, cols: Inclusive): LazyMatrix = ???

  override def update(row: Int, col: Int, value: Double): LazyMatrix = ???

  override def update(row: Int, col: :::.type, vec: LazyVector): LazyMatrix = ???

  override def update(row: :::.type, col: Int, vec: LazyVector): LazyMatrix = ???

  override def update(rows: Inclusive, cols: :::.type, mat: LazyMatrix): LazyMatrix = ???

  override def update(rows: :::.type, cols: Inclusive, mat: LazyMatrix): LazyMatrix = ???

  override def update(rows: Inclusive, cols: Inclusive, mat: LazyMatrix): LazyMatrix = ???

  override def +(that: Double): LazyMatrix = new LazyMatrix(BinOp("+", this.tree, Scalar(that)))

  override def -(that: Double): LazyMatrix = new LazyMatrix(BinOp("-", this.tree, Scalar(that)))

  override def *(that: Double): LazyMatrix = new LazyMatrix(BinOp("*", this.tree, Scalar(that)))

  override def /(that: Double): LazyMatrix = new LazyMatrix(BinOp("/", this.tree, Scalar(that)))

  override def +(that: LazyVector): LazyMatrix = new LazyMatrix(BinOp("+", this.tree, that.tree))

  override def -(that: LazyVector): LazyMatrix = new LazyMatrix(BinOp("+", this.tree, that.tree))

  override def *(that: LazyVector): LazyMatrix = new LazyMatrix(BinOp("+", this.tree, that.tree))

  override def /(that: LazyVector): LazyMatrix = new LazyMatrix(BinOp("+", this.tree, that.tree))

  override def +(that: LazyMatrix): LazyMatrix = new LazyMatrix(BinOp("+", this.tree, that.tree))

  override def -(that: LazyMatrix): LazyMatrix = new LazyMatrix(BinOp("-", this.tree, that.tree))

  override def *(that: LazyMatrix): LazyMatrix = new LazyMatrix(BinOp("*", this.tree, that.tree))

  override def /(that: LazyMatrix): LazyMatrix = new LazyMatrix(BinOp("/", this.tree, that.tree))

  override def %*%(that: LazyMatrix): LazyMatrix = ???

  override def %*%(that: LazyVector): Vector = ???

  override def t: LazyMatrix = ???

  override def ^(n: Int): LazyMatrix = ???

  override def map(f: (Double) => Double): LazyMatrix = ???

  /**
    * Reshapes the [[Matrix]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows  number of rows of the new matrix
    * @param cols  number of columns of the new matrix
    * @param byRow if true, LazyMatrix is reshaped my row
    * @return new LazyMatrix with the new dimensions and rearranged values
    */
  override def reshape(rows: Int, cols: Int, byRow: Boolean): LazyMatrix = ???

  override def copy: LazyMatrix = ???

  override def toDataFrame: DataFrame = ???
}
