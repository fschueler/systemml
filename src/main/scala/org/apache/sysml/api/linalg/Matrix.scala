/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysml.api.linalg

import org.apache.spark.sql.DataFrame
import org.apache.sysml.api.linalg.api.:::
import org.apache.sysml.api.mlcontext.{BinaryBlockMatrix, MLContextConversionUtil}
import org.apache.sysml.runtime.controlprogram.caching.MatrixObject

import scala.util.Random

// TODO: sparsity
// TODO: make matrix generic? what are possible value types?

/**
  * Matrix class for SystemML
  *
  * Represents the matrix that will be translated to SystemML's matrix type.
  *
  * @param impl the underlying matrix to support numerical computations in Scala
  * @param nrow number of rows of the matrix
  * @param ncol number of columns of the matrix
  */
class Matrix protected(val impl: Array[Double],
                       val nrow: Int,
                       val ncol: Int,
                       val matob: MatrixObject = null) {

  //////////////////////////////////////////
  // Constructors
  //////////////////////////////////////////

  //////////////////////////////////////////
  // Accessors
  //////////////////////////////////////////

  def apply(row: Int, col: Int): Double = ???

  def apply(row: Int, col: :::.type): Matrix = ???

  def apply(row: :::.type, col: Int): Matrix = ???

  def apply(rows: Range, cols: :::.type): Matrix = ???

  def apply(rows: :::.type, cols: Range): Matrix = ???

  def apply(rows: Range, cols: Range): Matrix = ???

  def apply(rows: Range, cols: Int): Matrix = ???

  def apply(rows: Int, cols: Range): Matrix = ???

  //////////////////////////////////////////
  // Left Indexing assignments
  //////////////////////////////////////////

  // TODO make sure that the orientation of the vector (row/col) fits the assignment
  def update(row: Int, col: Int, value: Double): Matrix = ???

  def update(row: Int, col: :::.type, vec: Matrix): Matrix = ???

  def update(row: :::.type, col: Int, vec: Matrix): Matrix = ???

  def update(rows: Range, cols: :::.type, mat: Matrix): Matrix = ???

  def update(rows: :::.type, cols: Range, mat: Matrix): Matrix = ???

  def update(rows: Range, cols: Range, mat: Matrix): Matrix = ???

  def update(rows: Int, cols: Range, mat: Matrix): Matrix = ???

  def update(rows: Range, cols: Int, mat: Matrix): Matrix = ???

  //////////////////////////////////////////
  // M o scalar
  //////////////////////////////////////////

  def +(that: Double): Matrix = ???

  def -(that: Double): Matrix = ???

  def *(that: Double): Matrix = ???

  def /(that: Double): Matrix = ???

  //////////////////////////////////////////
  // columnwise M o vector (broadcast operators)
  //////////////////////////////////////////

//  private def broadcastRows(mat: Matrix, vec: Vector, op: (Double, Double) => Double) = ???
//
//  private def broadcastCols(mat: Matrix, vec: Vector, op: (Double, Double) => Double) = ???
//
//  private def broadcast(mat: Matrix, vec: Vector)(op: (Double, Double) => Double) = ???
//
//  def +(that: Vector): Matrix = broadcast(this, that)(_ + _)
//
//  def -(that: Vector): Matrix = broadcast(this, that)(_ - _)
//
//  def *(that: Vector): Matrix = broadcast(this, that)(_ * _)
//
//  def /(that: Vector): Matrix = broadcast(this, that)(_ / _)

  //////////////////////////////////////////
  // cellwise M o M
  //////////////////////////////////////////

  def +(that: Matrix): Matrix = ???

  def -(that: Matrix): Matrix = ???

  def *(that: Matrix): Matrix = ???

  def /(that: Matrix): Matrix = ???

  //////////////////////////////////////////
  // M x M -> M and  M x V -> V
  //////////////////////////////////////////

  def %*%(that: Matrix): Matrix = ???

//  def %*%(that: Vector): Vector = ???

  //////////////////////////////////////////
  // M operation
  //////////////////////////////////////////

  def t: Matrix = ???

  def ^(n: Int): Matrix = ???

  //////////////////////////////////////////
  // Convenience Transformations (Only to be used outside the macro)
  //////////////////////////////////////////

  def toBinaryBlockMatrix(): BinaryBlockMatrix = ???
  def toMatrixObject(): MatrixObject = matob
  def toDF(): DataFrame = ???

  /**
    * Returns the values of the matrix. If the matrix was evaluated in SystemML, it will be fetched.
    * NOTICE: this will always fetch the values again from the matrixObject!
    */
  private def getValues: Array[Array[Double]] = {
    if (matob != null) { // if matob and impl are != null, the most accurate should be matob
      val rows = matob.getNumRows.toInt
      val cols = matob.getNumColumns.toInt
      val out = MLContextConversionUtil.matrixObjectTo2DDoubleArray(matob)
      out
    } else if (impl != null) {
      to2D(impl)
    } else {
      throw new RuntimeException("Matrix has no values!")
    }
  }

  /**
    * Convert a 1D row-major order array to a 2D array.
    *
    * @param values The Array to be converted.
    * @return The same values reorganized into a 2D Array of rows.
    */
  private def to2D(values: Array[Double]): Array[Array[Double]] = {
    val out = Array.fill(this.nrow, this.ncol)(0.0)
    for (i <- 0 until nrow; j <- 0 until ncol) {
      out(i)(j) = values(i* ncol + j)
    }
    out
  }


  override def equals(that: Any): Boolean = that match {
    case m: Matrix => {
      val zipped = this.getValues.zip(m.getValues)
      val sameElems = zipped.map(x => x._1.sameElements(x._2)).fold(true)(_ && _)
      sameElems && this.nrow == m.nrow && this.ncol == m.ncol
    }
    case _ => false
  }
  override def hashCode(): Int = this.getValues.hashCode() + this.nrow + this.ncol

  override def toString: String = {
    val m = 3
    val n = 10
    s"""
       |Printing first $m rows, $n cols:
       |values:
       |${getValues.map(_.take(n).mkString(", ")).take(m).mkString("\n")}
       |nrow:   $nrow
       |ncol:   $ncol
       |hasMO:  ${matob != null}
     """.stripMargin
  }
}

object Matrix {

  /**
    * This should be the primary way of constructing a [[Matrix]] from an [[Array]] of values.
    * The [[Matrix]] is constructed row-major order, i.e. the [[Array]] (1, 2, 1, 2) with dimensions (2,2) will
    * generate the [[Matrix]]
    *   1 2
    *   1 2
    * @param impl The values that will be assignmed to the cells of the matrix in row-major order
    * @param rows Number of rows that the matrix should have.
    * @param cols Number of columns that the matrix should have. Note that rows * cols must be equal to impl.length.
    * @return a [[Matrix]] with values as cell entries and dimensionality (rows, cols)
    */
  def apply(impl: Array[Double], rows: Int, cols: Int): Matrix = new Matrix(impl, rows, cols)

  def fromDataFrame(df: DataFrame): Matrix = ???

  private[sysml] def fill(rows: Int, cols: Int)(gen: (Int, Int) => Double): Matrix = {
    require(rows * cols < Int.MaxValue)
    val array = new Array[Double](rows * cols)
    for (i <- 0 until rows; j <- 0 until cols) {
      array((i * cols) + j) = gen(i, j)
    }
    new Matrix(array, rows, cols)
  }

  def zeros(rows: Int, cols: Int): Matrix = fill(rows, cols)((i, j) => 0.0)
  def ones(rows: Int, cols: Int): Matrix = fill(rows, cols)((i, j) => 1.0)

  // TODO: support more parameters (min, max, distribution, sparsity, seed)
  def rand(rows: Int, cols: Int): Matrix = fill(rows, cols)((i, j) => Random.nextDouble())

  /** generate matrix with the vector on the diagonal */
  def diag(value: Double, length: Int): Matrix = fill(length, length)((i, j) => if (i == j) value else 0.0)

  /**
    * Reshapes the [[Matrix]] into a new format. cols * rows must equal the original number of elements.
    *
    * A matrix A of the form
    *
    * 1 3
    * 2 4
    *
    * will be reshaped into
    *
    * 1
    * 2
    * 3
    * 4
    *
    * by using Matrix.reshape(A, 4, 1)
    *
    * @param mat the matrix to be reshaped
    * @param rows number of rows of the new matrix
    * @param cols number of columns of the new matrix
    * @return new matrix with the new dimensions and rearranged values
    */
  def reshape(mat: Matrix, rows: Int, cols: Int): Matrix = ???

}

