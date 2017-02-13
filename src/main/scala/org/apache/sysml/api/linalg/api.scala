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

import org.apache.sysml.compiler.macros.RewriteMacros
import scala.language.experimental.macros

package object api {
  import Format._

  /**
    * The entry point for the systemML macro
    */
  final def systemml[T](e: T): SystemMLAlgorithm[T] = macro RewriteMacros.impl[T]

  // whole row/column selector
  object :::


  ////////////////////////////////////////////////
  // BUILTIN FUNCTIONS
  ///////////////////////////////////////////////

  /**
    * Read a matrix from a file. Supported formats currently include csv and binary.
    *
    * @param path Path to the file on disk or hdfs.
    * @param format File format (CSV, BINARY).
    * @return Matrix that contains values read from the file.
    */
  def read(path: String, format: FileFormat): Matrix = ???

  /**
    * Write matrix to disc in specified format. Supported formats currently include csv and binary.
    *
    * @param mat The matrix that should be written to disk or hdfs.
    * @param path Path to write the matrix on disk or hdfs. Use file:// or hdfs:// to specify protocol.
    * @param format File format to write matrix data.
    */
  def write(mat: Matrix, path: String, format: Format.FileFormat): Unit = ???

  /**
    * Column-wise matrix concatenation. Concatenates the second matrix as additional columns to the first matrix.
    * If the first matrix is of dimensions m x n, the second matrix must be of dimensions m x o, i.e. have the same
    * number of rows as the first. The resulting matrix will be of dimensions m x (n + o).
    *
    * @param mat1 First (left) matrix m x n.
    * @param mat2 Second (right) matrix m x o.
    * @return Concatenated [left, right] matrix m x (n + o).
    */
  def cbind(mat1: Matrix, mat2: Matrix): Matrix = ???

  /**
    * Finds the smallest value in a matrix.
    *
    * @param mat Input matrix.
    * @return Smallest value in input matrix.
    */
  def min(mat: Matrix): Double = ???

  /**
    * Finds the largest value in a matrix.
    *
    * @param mat Input matrix.
    * @return Largest value in input matrix.
    */
  def max(mat: Matrix): Double = ???

  /**
    * Compares the values in corresponding matrix cells and returns a matrix containing the smaller values in this
    * comparison. Example: min([1, 2, 3], [2, 1, 0]) = [1, 1, 0]
    *
    * @param matA Matrix.
    * @param matB Matrix.
    * @return Matrix containing the smaller values from cellwise comparison of matA and matB.
    */
  def min(matA: Matrix, matB: Matrix): Matrix = ???

  /**
    * Compares the values in corresponding matrix cells and returns a matrix containing the larger values in this
    * comparison. Example: max([1, 2, 3], [2, 1, 0]) = [2, 2, 3]
    *
    * @param matA Matrix.
    * @param matB Matrix.
    * @return Matrix containing the larger values from cellwise comparison of matA and matB.
    */
  def max(matA: Matrix, matB: Matrix): Matrix = ???

  /**
    * Compares the values in the matrix to the given double value and returns a matrix containing the smaller value of
    * this comparison. Example: min([1, 4, 2], 3) = [1, 3, 2]
    *
    * @param mat Matrix.
    * @param b Double.
    * @return Matrix containing the smaller values from cellwise comparison of matA and b.
    */
  def min(mat: Matrix, b: Double): Matrix = ???

  /**
    * Compares the values in the matrix to the given double value and returns a matrix containing the larger value of
    * this comparison. Example: max([1, 4, 2], 3) = [3, 4, 3]
    * @param mat Matrix.
    * @param b Double.
    * @return Matrix containing the larger values from cellwise comparison of matA and b.
    */
  def max(mat: Matrix, b: Double): Matrix = ???

  /**
    * Compares doubles a and b and returns the smaller value.
    *
    * @param a Double.
    * @param b Double.
    * @return a, if a < b, b else.
    */
  def min(a: Double, b: Double): Double = ???

  /**
    * Compares doubles a and b and returns the larger value.
    *
    * @param a Double.
    * @param b Double.
    * @return a, if a > b, b else.
    */
  def max(a: Double, b: Double): Double = ???

  /**
    * Computes the product of all cells in the matrix.
    * Example: prod([1.0, 2.0, 3.0]) = 6.0
    *
    * @param mat Input Matrix.
    * @return Product of all cells of the input matrix.
    */
  def prod(mat: Matrix): Double = ???

  /**
    * Row-wise matrix concatenation. Concatenates the second matrix as additional rows to the first matrix.
    * Example: rbind([1, 2, 3], [4, 5, 6]) = [1, 2, 3]
    *                                        [4, 5, 6]
    *
    * @param top Matrix of which the rows will be on the top.
    * @param bottom Matrix of which the rows will be appended to the bottom of the top matrix.
    * @return Matrix which consists of the rows of the bottom matrix appended to the top matrix.
    */
  def rbind(top: Matrix, bottom: Matrix): Matrix = ???

  /**
    * Removes all empty rows or columns from the input matrix target X according to the specified margin.
    * Also, allows to apply a filter F before removing the empty rows/cols.
    *
    */
  def removeEmpty(target: Matrix, margin: Margin.MarginSelector, select: Boolean) = ???
  // TODO add test

  /**
    * Replace values that equal the pattern with the respective replacement value.
    * Example: pattern NaN can be replaced by another Double value.
    *
    * @param target The input matrix.
    * @param pattern Cell values that will be replaced.
    * @param replacement Replacement value for matching pattern values.
    * @return The input matrix with cell values equal to the pattern value replaced by the replacement value.
    */
  def replace(target: Matrix, pattern: Double, replacement: Double): Matrix = ???

  /**
    * Reverses the columns in a matrix.
    * Example: reverse([1, 2, 3]    [4, 5, 6]
    *                  [4, 5, 6]) = [1, 2, 3]
    *
    * @param target Input matrix.
    * @return Input matrix with reversed rows.
    */
  def rev(target: Matrix): Matrix = ???

  /**
    * Computes the sum of all elements in the matrix.
    *
    * @param mat Input matrix.
    * @return Sum of all elements in the matrix.
    */
  def sum(mat: Matrix): Double = ???

  /**
    * Parallel minimum. Computes cell-wise minimum between the cells of matrix A and B.
    *
    * @param matA Left input matrix.
    * @param matB Right input matrix.
    * @return Matrix containing the smaller values of the cell comparisons.
    */
  def pmin(matA: Matrix, matB: Matrix): Matrix = ???

  /**
    * Parallel minimum. Computes cell-wise minimum between the cells of matrix A and a double value.
    *
    * @param matA Input matrix.
    * @param value Input value.
    * @return Cell-wise minimum between cell entries of the input matrix and the provided value.
    */
  def pmin(matA: Matrix, value: Double): Matrix = ???

  /**
    * Parallel maximum. Computes cell-wise maximum between cells of matrix A and B.
    *
    * @param matA Left input matrix.
    * @param matB Right input matrix.
    * @return Matrix containing the larger values of the cell comparisons.
    */
  def pmax(matA: Matrix, matB: Matrix): Matrix = ???

  /**
    * Parallel maximum. Computes cell-wise maximum between the cells of matrix A and a double value.
    *
    * @param matA Input matrix.
    * @param value Input value.
    * @return Cell-wise maximum between cell entries of the input matrix and the provided value.
    */
  def pmax(matA: Matrix, value: Double): Matrix = ???

  /**
    * Find the column index of the smallest value in each row.
    * NOTE: Since DML is 1-based, the returned indices will be 1-based!
    *
    * @param mat Input Matrix.
    * @return Column vector containing the column-indices of the minimum value of each row.
    */
  def rowIndexMin(mat: Matrix): Matrix = ???

  /**
    * Find the column index of the largest value in each row.
    * NOTE: Since DML is 1-based, the returned indices will be 1-based!
    *
    * @param mat Input Matrix.
    * @return Column vector containing the column-indices of the minimum value of each row.
    */
  def rowIndexMax(mat: Matrix): Matrix = ???

  def rowSums(mat: Matrix): Matrix = ???

  def colSums(mat: Matrix): Matrix = ???

  def mean(mat: Matrix): Double = ???

  def rowMeans(mat: Matrix): Matrix = ???

  def colMeans(mat: Matrix): Matrix = ???

  def log(x: Double): Double = ???

  def log(mat: Matrix): Matrix = ???

  def abs(x: Double): Double = ???

  def exp(b: Matrix): Matrix = ???

  def ppred(mat: Matrix, s: Double, op: String): Matrix = ???

  ///////////////////////////////////
  // Implicit Matrix and Matrix Ops
  ///////////////////////////////////

  /** This allows operations with Matrixs and Matrices as left arguments such as Double * Matrix */
  implicit class MatrixOps(private val n: Double) extends AnyVal {
    def +(v: Matrix): Matrix = v + n

    def -(v: Matrix): Matrix = v - n

    def *(v: Matrix): Matrix = v * n

    def /(v: Matrix): Matrix = v / n
  }

  object Format {
    sealed trait FileFormat
    case object CSV extends FileFormat
    case object BINARY extends FileFormat
  }

  object Margin {
    sealed trait MarginSelector
    case object Rows extends MarginSelector
    case object Cols extends MarginSelector
  }
}
