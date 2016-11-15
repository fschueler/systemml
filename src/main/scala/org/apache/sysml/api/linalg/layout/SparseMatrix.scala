package org.apache.sysml.api.linalg.layout

import org.apache.sysml.api.linalg.Distributions.{Distribution, Normal}
import org.apache.sysml.api.linalg.layout.local.LocalCOOMatrix

abstract class SparseMatrix(override val rows: Int,
                            override val cols: Int,
                            override var transposed: Boolean = false) extends Matrix {

  override def equals(obj: scala.Any): Boolean
}

object SparseMatrix extends MatrixConstructors {
  override def rand(rows: Int, cols: Int, dist: Distribution): Matrix = {
    val randomData = Map.empty[(Int, Int), Double]

    new LocalCOOMatrix(rows, cols, transpose = false, randomData)
  }

  def rand(rows: Int, cols: Int, sparsity: Double): Matrix = {
    rand(rows, cols, dist = Normal())
  }

  override def zeros(rows: Int, cols: Int): Matrix = ???

  override def eye(elemens: Int): Matrix = ???

  override def apply(vals: Array[Array[Double]]): Matrix = ???
}
