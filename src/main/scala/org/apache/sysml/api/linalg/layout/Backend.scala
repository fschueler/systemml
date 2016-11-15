package org.apache.sysml.api.linalg.layout

trait Backend {

  /**
    * Cellwise Addition Operations
    */
  def plus_dd(left: DenseMatrix,  right: DenseMatrix):  Matrix
  def plus_ds(left: DenseMatrix,  right: SparseMatrix): Matrix
  def plus_sd(left: SparseMatrix, right: DenseMatrix):  Matrix
  def plus_ss(left: SparseMatrix, right: SparseMatrix): Matrix

  /**
    * Cellwise Multiplication Operations
    */
  def times_dd(left: DenseMatrix, right: DenseMatrix): Matrix
  def times_ds(left: DenseMatrix, right: SparseMatrix): Matrix
  def times_sd(left: SparseMatrix, right: DenseMatrix): Matrix
  def times_ss(left: SparseMatrix, right: SparseMatrix): Matrix

  /**
    * Matrix Multiplication Operations
    */
  def dot_dd(left: DenseMatrix, right: DenseMatrix): Matrix
  def dot_ds(left: DenseMatrix, right: SparseMatrix): Matrix
  def dot_sd(left: SparseMatrix, right: DenseMatrix): Matrix
  def dot_ss(left: SparseMatrix, right: SparseMatrix): Matrix
}
