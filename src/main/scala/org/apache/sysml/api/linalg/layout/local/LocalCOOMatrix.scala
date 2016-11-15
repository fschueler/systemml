package org.apache.sysml.api.linalg.layout.local

import org.apache.sysml.api.linalg.layout.{Matrix, SparseMatrix}

class LocalCOOMatrix(override val rows: Int,
                     override val cols: Int,
                     val transpose: Boolean = false,
                     override val data: Map[(Int, Int), Double]) extends SparseMatrix(rows, cols, transpose) with SystemMLLocalBackend {

  override type MatrixData = Map[(Int, Int), Double]

  override def +(that: Matrix): Matrix = ???

  override def t: LocalCOOMatrix.this.type = ???

  override def %*%(that: Matrix): Matrix = ???

  override def -(that: Matrix): Matrix = ???

  override def *(that: Matrix): Matrix = ???

  override def /(that: Matrix): Matrix = ???

  override def equals(obj: scala.Any): Boolean = ???
}
