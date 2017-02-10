package org.apache.sysml.api.linalg

object Vector {
  /**
    * Create a Matrix with one row
    * @param values
    * @return
    */
  def apply(values: Array[Double]): Matrix = {
    Matrix(values, values.length, 1)
  }

  def rand(length: Int): Matrix = {
    Matrix.rand(length, 1)
  }

  def zeros(length: Int): Matrix = {
    Matrix.zeros(length, 1)
  }

  def ones(length: Int): Matrix = {
    Matrix.ones(length, 1)
  }
}
