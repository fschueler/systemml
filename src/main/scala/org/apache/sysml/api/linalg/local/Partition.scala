package org.apache.sysml.api.linalg.local

import org.apache.sysml.api.linalg.MatrixOps
import org.apache.sysml.api.linalg.api.:::
import org.apache.sysml.runtime.matrix.data.LibMatrixMult

import scala.collection.immutable.Range.Inclusive

abstract class Partition[A](val data: A) extends MatrixOps[Partition[A], Partition[A]] {

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

class Dense(override val data: Array[Double], override val rows: Int, override val cols: Int) extends Partition[Array[Double]](data) {

  override def apply(row: Int, col: Int): Double = data(toRowMajor(row, col))

  override def apply(row: Int, col: :::.type): Partition[Array[Double]] = ???

  override def apply(row: :::.type, col: Int): Partition[Array[Double]] = ???

  override def apply(rows: Inclusive, cols: :::.type): Partition[Array[Double]] = ???

  override def apply(rows: :::.type, cols: Inclusive): Partition[Array[Double]] = ???

  override def apply(rows: Inclusive, cols: Inclusive): Partition[Array[Double]] = ???

  override def update(row: Int, col: Int, value: Double): Partition[Array[Double]] = ???

  override def update(row: Int, col: :::.type, vec: Partition[Array[Double]]): Partition[Array[Double]] = ???

  override def update(row: :::.type, col: Int, vec: Partition[Array[Double]]): Partition[Array[Double]] = ???

  override def update(rows: Inclusive, cols: :::.type, mat: Partition[Array[Double]]): Partition[Array[Double]] = ???

  override def update(rows: :::.type, cols: Inclusive, mat: Partition[Array[Double]]): Partition[Array[Double]] = ???

  override def update(rows: Inclusive, cols: Inclusive, mat: Partition[Array[Double]]): Partition[Array[Double]] = ???

  override def +(that: Double): Partition[Array[Double]] = new Dense(data.map(_ + that), rows, cols)

  override def -(that: Double): Partition[Array[Double]] = new Dense(data.map(_ - that), rows, cols)

  override def *(that: Double): Partition[Array[Double]] = new Dense(data.map(_ * that), rows, cols)

  override def /(that: Double): Partition[Array[Double]] = new Dense(data.map(_ / that), rows, cols)

  override def +(that: Partition[Array[Double]]): Partition[Array[Double]] = {
    val newData = Array.fill(rows * cols)(0.0)
    for (i <- rows * cols) {
      newData(i) = data(i) + that.data(i)
    }
    new Dense(newData, rows, cols)
  }

  override def -(that: Partition[Array[Double]]): Partition[Array[Double]] = ???

  override def *(that: Partition[Array[Double]]): Partition[Array[Double]] = ???

  override def /(that: Partition[Array[Double]]): Partition[Array[Double]] = ???

  override def %*%(that: Partition[Array[Double]]): Partition[Array[Double]] = ???

  override def t: Partition[Array[Double]] = ???

  override def ^(n: Int): Partition[Array[Double]] = ???

  /**
    * Reshapes the [[Partition]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows  number of rows of the new A
    * @param cols  number of columns of the new A
    * @param byRow if true, Ais reshaped my row
    * @return new Awith the new dimensions and rearranged values
    */
  override def reshape(rows: Int, cols: Int, byRow: Boolean): Partition[Array[Double]] = ???

  override def copy: Partition[Array[Double]] = ???
}
