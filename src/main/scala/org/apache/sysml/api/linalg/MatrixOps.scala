package org.apache.sysml.api.linalg

import org.apache.sysml.api.linalg.api.:::

/**
  * Trait that defines operations between matrices (and vectors)
  *
  * @tparam V
  * @tparam M
  */
trait MatrixOps[V, M <: MatrixOps[V, M]] {
  // TODO should this be Long?
  def rows: Int
  def cols: Int

  def size: (Int, Int) = (rows, cols)

  //////////////////////////////////////////
  // Accessors
  //////////////////////////////////////////

  def apply(row: Int, col: Int): Double

  def apply(row: Int, col: :::.type ): V

  def apply(row: :::.type, col: Int): V

  def apply(rows: Range.Inclusive, cols: :::.type): M

  def apply(rows: :::.type, cols: Range.Inclusive): M

  def apply(rows: Range.Inclusive, cols: Range.Inclusive): M

  //////////////////////////////////////////
  // Left Indexing assignments
  //////////////////////////////////////////

  def update(row: Int, col: Int, value: Double): M

  def update(row: Int, col: :::.type, vec: V): M

  def update(row: :::.type, col: Int, vec: V): M

  def update(rows: Range.Inclusive, cols: :::.type, mat: M): M

  def update(rows: :::.type, cols: Range.Inclusive, mat: M): M

  def update(rows: Range.Inclusive, cols: Range.Inclusive, mat: M): M

  //////////////////////////////////////////
  // A o scalar
  //////////////////////////////////////////

  def +(that: Double): M

  def -(that: Double): M

  def *(that: Double): M

  def /(that: Double): M

  //////////////////////////////////////////
  // columnwise M o vector (broadcast operators)
  //////////////////////////////////////////

  def +(that:  V): M

  def -(that:  V): M

  def *(that:  V): M

  def /(that:  V): M

  //////////////////////////////////////////
  // cellwise A o A
  //////////////////////////////////////////

  def +(that: M): M

  def -(that: M): M

  def *(that: M): M

  def /(that: M): M

  //////////////////////////////////////////
  // A x A -> A and  A x A -> A
  //////////////////////////////////////////

  def %*%(that: M): M

  def %*%(that: V): V

  //////////////////////////////////////////
  // A operation
  //////////////////////////////////////////

  def t: M

  def ^(n: Int): M

  /**
    * Reshapes the [[Matrix]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows number of rows of the new A
    * @param cols number of columns of the new A
    * @param byRow if true, Ais reshaped my row
    * @return new Awith the new dimensions and rearranged values
    */
  def reshape(rows: Int, cols: Int, byRow: Boolean = true): M

  def copy: M
}
