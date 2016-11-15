package org.apache.sysml.api.linalg.layout

import org.apache.sysml.api.linalg.Distributions.Distribution
import org.apache.sysml.api.linalg.layout.local.LocalDenseMatrix

abstract class DenseMatrix(override val rows: Int,
                           override val cols: Int,
                           override var transposed: Boolean = false) extends Matrix {

  type MatrixData = Array[Double]

  override def +(that: Matrix): Matrix = that match {
    case dm: DenseMatrix => this + dm
    case sm: SparseMatrix => this + sm
  }

  def +(denseMatrix: DenseMatrix): Matrix
  def +(sparseMatrix: SparseMatrix): Matrix

  override def equals(that: Any): Boolean
}

object DenseMatrix extends MatrixConstructors {
  override def rand(rows: Int, cols: Int, dist: Distribution): Matrix = {
    new LocalDenseMatrix(rows, cols, transpose = false, Array.fill(rows * cols)(dist.sample))
  }

  override def zeros(rows: Int, cols: Int): Matrix = ???

  override def eye(elemens: Int): Matrix = ???

  override def apply(vals: Array[Array[Double]]): Matrix = new LocalDenseMatrix(vals.length, vals(0).length, false, vals.flatten)
}
