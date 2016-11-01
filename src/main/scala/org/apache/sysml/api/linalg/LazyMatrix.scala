package org.apache.sysml.api.linalg
import org.apache.spark.sql.DataFrame
import org.apache.sysml.api.linalg.Lazy.{Application, BinOp, Scalar, Tree, UnaryOp}
import org.apache.sysml.api.linalg.api.:::
import org.apache.sysml.api.linalg.types.TypeClass.Layout
import org.apache.sysml.api.linalg.types.data.DataContainer

import scala.collection.immutable.Range.Inclusive

class LazyMatrix[A: Layout](override val impl: A) extends Matrix[A] with Lazy {
  //////////////////////////////////////////
  override def rows: Int = ???

  override def cols: Int = ???

  // TODO make sure that the orientation of the vector (row/col) fits the assignment
  override def update(row: Int, col: Int, value: Double): Matrix[A] = ???

  override def update(row: Int, col: :::.type, vec: Vector[A]): Matrix[A] = ???

  override def update(row: :::.type, col: Int, vec: Vector[A]): Matrix[A] = ???

  override def update(rows: Inclusive, cols: :::.type, mat: Matrix[A]): Matrix[A] = ???

  override def update(rows: :::.type, cols: Inclusive, mat: Matrix[A]): Matrix[A] = ???

  override def update(rows: Inclusive, cols: Inclusive, mat: Matrix[A]): Matrix[A] = ???

  override def +(that: Double): Matrix[A] = ???

  override def -(that: Double): Matrix[A] = ???

  override def *(that: Double): Matrix[A] = ???

  override def /(that: Double): Matrix[A] = ???

  override def +(that: Vector[A]): Matrix[A] = ???

  override def -(that: Vector[A]): Matrix[A] = ???

  override def *(that: Vector[A]): Matrix[A] = ???

  override def /(that: Vector[A]): Matrix[A] = ???

  override def +(that: Matrix[A]): Matrix[A] = ???

  override def -(that: Matrix[A]): Matrix[A] = ???

  override def *(that: Matrix[A]): Matrix[A] = ???

  override def /(that: Matrix[A]): Matrix[A] = ???

  override def %*%(that: Matrix[A]): Matrix[A] = ???

  override def %*%(that: Vector[A]): Vector[A] = ???

  override def t: Matrix[A] = ???

  override def ^(n: Int): Matrix[A] = ???

  /**
    * Reshapes the [[Matrix]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows  number of rows of the new A
    * @param cols  number of columns of the new A
    * @param byRow if true, Ais reshaped my row
    * @return new Awith the new dimensions and rearranged values
    */
  override def reshape(rows: Int, cols: Int, byRow: Boolean): Matrix[A] = ???

  override def copy: Matrix[A] = ???

  override def toDataFrame: DataFrame = ???

  override def apply(row: Int, col: Int): Double = ???

  override def apply(row: Int, col: :::.type): Vector[A] = ???

  override def apply(row: :::.type, col: Int): Vector[A] = ???

  override def apply(rows: Inclusive, cols: :::.type): Matrix[A] = ???

  override def apply(rows: :::.type, cols: Inclusive): Matrix[A] = ???

  override def apply(rows: Inclusive, cols: Inclusive): Matrix[A] = ???
}
