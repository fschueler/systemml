package org.apache.sysml.api.linalg.data

abstract class Partition[A](val data: A, val rows: Int, val cols: Int) {

  def size: (Int, Int) = (rows, cols)

  def reshape(rows: Int, cols: Int, byRow: Boolean): Partition[A]

  def copy: Partition[A]

  /**
    * Calculates the one dimensional position for row-major data from row- and column-indices.
    * matrix[ i ][ j ] = array[ i*m + j ]
    *
    * @param i row index
    * @param j column index
    * @return linearized index for row-major data
    */
  def toRowMajor(i: Int, j: Int): Int = i * rows + j
}