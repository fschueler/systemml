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

  def sum(mat: Matrix): Double = ???

  def rowSums(mat: Matrix): Matrix = ???

  def colSums(mat: Matrix): Matrix = ???

  def mean(mat: Matrix): Double = ???

  def rowMeans(mat: Matrix): Matrix = ???

  def colMeans(mat: Matrix): Matrix = ???

  def log(x: Double): Double = ???

  def log(mat: Matrix): Matrix = ???

  def abs(x: Double): Double = ???

  def exp(b: Matrix): Matrix = ???

  def rowIndexMax(mat: Matrix): Matrix = ???

  def pmax(mat: Matrix, s: Double): Matrix = ???

  def ppred(mat: Matrix, s: Double, op: String): Matrix = ???

  def min(mat: Matrix): Double = ???

  def max(Mat: Matrix): Double = ???

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
}
