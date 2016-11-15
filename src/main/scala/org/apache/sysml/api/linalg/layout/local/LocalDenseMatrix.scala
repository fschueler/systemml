package org.apache.sysml.api.linalg.layout.local

import org.apache.sysml.api.linalg.Distributions.Distribution
import org.apache.sysml.api.linalg.layout.{DenseMatrix, Matrix, MatrixConstructors, SparseMatrix}

class LocalDenseMatrix(override val rows: Int,
                       override val cols: Int,
                       val transpose: Boolean,
                       override val data: Array[Double]) extends DenseMatrix(rows, cols, transpose) with SystemMLLocalBackend {

  override def +(that: DenseMatrix): Matrix = (this, that) match {
    case (left: LocalDenseMatrix, right: LocalDenseMatrix) => backend.plus_dd(left, right)
  }

  override def +(that: SparseMatrix): Matrix = (this, that) match {
    case (left: LocalDenseMatrix, right: LocalCOOMatrix) => backend.plus_ds(left, right)
  }

  override def %*%(that: Matrix): Matrix = that match {
    case dm: DenseMatrix => this %*% dm
  }

  def %*%(that: DenseMatrix): Matrix = backend.dot_dd(this, that)

  def %*%(that: SparseMatrix): Matrix = backend.dot_ds(this, that)

  override def -(that: Matrix): Matrix = ???

  override def *(that: Matrix): Matrix = ???

  override def /(that: Matrix): Matrix = ???

  override def t: LocalDenseMatrix.this.type = {
    this.transposed = !this.transposed
    this
  }

  override def equals(that: Any): Boolean = that match {
    case m: LocalDenseMatrix => m.data.sameElements(this.data) && m.rows == this.rows && m.cols == this.cols
    case _ => false
  }
}

object LocalDenseMatrix extends MatrixConstructors {
  override def apply(vals: Array[Array[Double]]): Matrix = ???

  override def rand(rows: Int, cols: Int, dist: Distribution): Matrix = {
    new LocalDenseMatrix(rows, cols, transpose = false, Array.fill(rows * cols)(dist.sample))
  }

  override def zeros(rows: Int, cols: Int): Matrix = ???

  override def eye(elemens: Int): Matrix = ???
}
