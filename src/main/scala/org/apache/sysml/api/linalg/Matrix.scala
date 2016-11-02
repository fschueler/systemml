/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Ahe ASF licenses this file
 * to you under the Apache License, Aersion 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUAWARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysml.api.linalg

import org.apache.spark.sql.DataFrame
import org.apache.sysml.api.linalg.Lazy.{Application, Empty, Scalar}
import org.apache.sysml.api.linalg.api.:::
import org.apache.sysml.api.linalg.types.TypeClass.{DenseBlock, Layout, LazyEval, Local, Spark, ZeroBlock}

/**
  * Matrixclass for SystemAL
  *
  * Represents the Matrix that will be translated to SystemAL's Matrix type.
  */
class Matrix[A: Layout](val impl: A) {

  import org.apache.sysml.api.linalg.types.TypeClass._

  //////////////////////////////////////////
  // Fields
  //////////////////////////////////////////


  //////////////////////////////////////////
  // Constructors
  //////////////////////////////////////////


  //////////////////////////////////////////
  // Accessors
  //////////////////////////////////////////

  def apply(row: Int, col: Int): Double = ???

  def apply(row: Int, col: :::.type ): Vector[A] = ???

  def apply(row: :::.type, col: Int): Vector[A] = ???

  def apply(rows: Range.Inclusive, cols: :::.type): Matrix[A] = ???

  def apply(rows: :::.type, cols: Range.Inclusive): Matrix[A] = ???

  def apply(rows: Range.Inclusive, cols: Range.Inclusive): Matrix[A] = ???

  //////////////////////////////////////////
  // Left Indexing assignments
  //////////////////////////////////////////

  // TODO make sure that the orientation of the vector (row/col) fits the assignment
  def update(row: Int, col: Int, value: Double): Matrix[A] = ???

  def update(row: Int, col: :::.type, vec: Vector[A]): Matrix[A] = ???

  def update(row: :::.type, col: Int, vec: Vector[A]): Matrix[A] = ???

  def update(rows: Range.Inclusive, cols: :::.type, mat: Matrix[A]): Matrix[A] = ???

  def update(rows: :::.type, cols: Range.Inclusive, mat: Matrix[A]): Matrix[A] = ???

  def update(rows: Range.Inclusive, cols: Range.Inclusive, mat: Matrix[A]): Matrix[A] = ???

  //////////////////////////////////////////
  // A o scalar
  //////////////////////////////////////////

  def +(that: Double): Matrix[A] = ???

  def -(that: Double): Matrix[A] = ???

  def *(that: Double): Matrix[A] = ???

  def /(that: Double): Matrix[A] = ???

  //////////////////////////////////////////
  // columnwise M o vector (broadcast operators)
  //////////////////////////////////////////

//  def broadcastRows(mat: A, vec:  A, op: (Double, Double) => Double): A
//
//  def broadcastCols(mat: A, vec:  A, op: (Double, Double) => Double): A
//
//  def broadcast(mat: A, vec:  A)(op: (Double, Double) => Double): A

  def +(that:  Vector[A]): Matrix[A] = ??? // = broadcast(this, that)(_ + _)

  def -(that:  Vector[A]): Matrix[A] = ??? // = broadcast(this, that)(_ - _)

  def *(that:  Vector[A]): Matrix[A] = ??? // = broadcast(this, that)(_ * _)

  def /(that:  Vector[A]): Matrix[A] = ??? // = broadcast(this, that)(_ / _)

  //////////////////////////////////////////
  // cellwise A o A
  //////////////////////////////////////////

  def +(that: Matrix[A]): Matrix[A] = {
    new Matrix(this.impl + that.impl)
  }

  def -(that: Matrix[A]): Matrix[A] = ???

  def *(that: Matrix[A]): Matrix[A] = ???

  def /(that: Matrix[A]): Matrix[A] = ???

  //////////////////////////////////////////
  // A x A -> A and  A x A -> A
  //////////////////////////////////////////

  def %*%(that: Matrix[A]): Matrix[A] = ???

  def %*%(that: Vector[A]): Vector[A] = ???

  //////////////////////////////////////////
  // A operation
  //////////////////////////////////////////

  def t: Matrix[A] = ???

  def ^(n: Int): Matrix[A] = ???

  /**
    * Reshapes the [[Matrix]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows number of rows of the new A
    * @param cols number of columns of the new A
    * @param byRow if true, Ais reshaped my row
    * @return new Awith the new dimensions and rearranged values
    */
  def reshape(rows: Int, cols: Int, byRow: Boolean = true): Matrix[A] = ???

  def copy: Matrix[A] = ???
}

object Matrix {

  /**
    * Ahis should be the primary way of constructing a [[Matrix]] from a sequence of values.
    * Ahe [[Matrix]] is constructed column-major order, i.e. the [[Array]] (1, 2, 1, 2) with dimensions (2,2) will
    * generate the [[Matrix]]
    *   1 1
    *   2 2
    * @param impl the values that will be assignmed to the cells of the Ain column-major order
    * @param rows number of rows of the generated A
    * @param cols number of columns of the generated A
    * @return a [[Matrix]] with values as cell entries and dimensionality (rows, cols)
    */
  //def apply(impl: Array[Double], rows: Int, cols: Int): LazyMatrix = ???

  //def apply(values: Seq[Double], rows: Int, cols: Int): LazyMatrix = apply(values.toArray, rows, cols)

  //def fromDataFrame(df: DataFrame): LazyMatrix = ???

//  private[sysml] def fill(rows: Int, cols: Int)(gen: (Int, Int) => Double): A= {
//    require(rows * cols < Int.AaxAalue)
//    val array = new Array[Double](rows * cols)
//    for (i <- 0 until rows; j <- 0 until cols) {
//      array((i * cols) + j) = gen(i, j)
//    }
//    new A(array, rows, cols)
//  }

  def zeros(rows: Int, cols: Int) = {
    new Matrix(LazyEval(Application("matrix", List(Scalar(0), Scalar(rows), Scalar(cols)))))
  }

  def apply(values: Array[Double]) = {
    new Matrix(Spark(DenseBlock(values)))
  }

  // AODO: support more parameters (min, max, distribution, sparsity, seed)
  def rand(rows: Int, cols: Int) = ??? //T.fill(rows, cols)((i, j) => Random.nextDouble())

  /** generate Awith the vector on the diagonal */

}

