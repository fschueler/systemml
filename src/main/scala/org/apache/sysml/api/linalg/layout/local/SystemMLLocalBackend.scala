package org.apache.sysml.api.linalg.layout.local

import org.apache.sysml.api.linalg.Distributions.Distribution
import org.apache.sysml.api.linalg.layout.{Backend, DenseMatrix, Matrix, SparseMatrix}
import org.apache.sysml.runtime.matrix.data.{LibMatrixDatagen, LibMatrixMult, MatrixBlock}

trait SystemMLLocalBackend {
  val backend = SystemMLLocalBackend
}

object SystemMLLocalBackend extends Backend {

  /**
    * Transform a DenseMatrix to a MatrixBlock.
    *
    * This initializes a MatrixBlock with the values from the DenseMatrix. \
    * The result will be a dense MatrixBlock.
    *
    * @param dm the input DenseMatrix that will be converted to a MatrixBlock
    * @return the MatrixBlock containing the values of the input DenseMatrix
    */
  def toMatrixBlock(dm: DenseMatrix): MatrixBlock = {
    new MatrixBlock(dm.data, dm.rows, dm.cols)
  }

  /**
    * Return a new MatrixBlock.
    *
    * The returned MatrixBlock will not be initialized which means that it woll not contain data.
    *
    * @param rows Number of rows of the returned MatrixBlock
    * @param cols Number of columns of the returned MatrixBlock
    * @return
    */
  def getNewMatrixBlock(rows: Int, cols: Int): MatrixBlock = {
    new MatrixBlock(rows, cols, false)
  }

  /**
    * Transform a MatrixBlock into a DenseMatrix
    *
    * @param mb
    * @return
    */
  def toDenseMatrix(mb: MatrixBlock): Matrix = {
    new LocalDenseMatrix(mb.getNumRows, mb.getNumColumns, transpose = false, mb.getDenseBlock)
  }

  /**
    * Cellwise Addition Operations
    */
  override def plus_dd(left: DenseMatrix, right: DenseMatrix): Matrix = ???

  override def plus_ds(left: DenseMatrix, right: SparseMatrix): Matrix = ???

  override def plus_sd(left: SparseMatrix, right: DenseMatrix): Matrix = ???

  override def plus_ss(left: SparseMatrix, right: SparseMatrix): Matrix = ???

  /**
    * Cellwise Multiplication Operations
    */
  override def times_dd(left: DenseMatrix, right: DenseMatrix): Matrix = ???

  override def times_ds(left: DenseMatrix, right: SparseMatrix): Matrix = ???

  override def times_sd(left: SparseMatrix, right: DenseMatrix): Matrix = ???

  override def times_ss(left: SparseMatrix, right: SparseMatrix): Matrix = ???

  /**
    * Matrix Multiplication Operations
    */
  override def dot_dd(left: DenseMatrix, right: DenseMatrix): Matrix = {
    val A = toMatrixBlock(left)
    val B = toMatrixBlock(right)
    val C = getNewMatrixBlock(left.rows, right.cols)

    LibMatrixMult.matrixMult(A, B, C)

    toDenseMatrix(C)
  }

  override def dot_ds(left: DenseMatrix, right: SparseMatrix): Matrix = ???

  override def dot_sd(left: SparseMatrix, right: DenseMatrix): Matrix = ???

  override def dot_ss(left: SparseMatrix, right: SparseMatrix): Matrix = ???
}
