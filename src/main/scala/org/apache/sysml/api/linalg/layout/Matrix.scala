package org.apache.sysml.api.linalg.layout

trait Matrix {
  type MatrixData

  def rows: Int
  def cols: Int
  var transposed: Boolean

  def size: (Int, Int) = (rows, cols)
  val data: MatrixData

  def +(that: Matrix): Matrix
  def -(that: Matrix): Matrix
  def *(that: Matrix): Matrix
  def /(that: Matrix): Matrix

  def %*%(that: Matrix): Matrix

  def t: this.type

  override def equals(that: Any): Boolean

  override def hashCode(): Int = data.hashCode() * rows % cols
}
