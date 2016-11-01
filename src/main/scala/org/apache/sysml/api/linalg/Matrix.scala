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
import org.apache.sysml.api.linalg.Lazy.{Application, Empty}
import org.apache.sysml.api.linalg.api.:::
import org.apache.sysml.api.linalg.types.data.DataContainer

import scala.util.Random

/**
  * Matrixclass for SystemAL
  *
  * Represents the Matrix that will be translated to SystemAL's Matrix type.
  */
trait MatrixOps[M, V] {

  //////////////////////////////////////////
  // Fields
  //////////////////////////////////////////
  def rows: Int
  def cols: Int

  def impl: DataContainer[_]

  //////////////////////////////////////////
  // Constructors
  //////////////////////////////////////////


  //////////////////////////////////////////
  // Accessors
  //////////////////////////////////////////

  def apply(row: Int, col: Int): Double

  def apply(row: Int, col: :::.type ): V

  def apply(row: :::.type, col: Int): V

  def apply(rows: Range.Inclusive, cols: :::.type): M

  def apply(rows: :::.type, cols: Range.Inclusive): M

  def apply(rows: Range.Inclusive, cols: Range.Inclusive): M

  //////////////////////////////////////////
  // Left Indexing assignments
  //////////////////////////////////////////

  // TODO make sure that the orientation of the vector (row/col) fits the assignment
  def update(row: Int, col: Int, value: Double): M

  def update(row: Int, col: :::.type, vec: V): M

  def update(row: :::.type, col: Int, vec: V): M

  def update(rows: Range.Inclusive, cols: :::.type, mat: M): M

  def update(rows: :::.type, cols: Range.Inclusive, mat: M): M

  def update(rows: Range.Inclusive, cols: Range.Inclusive, mat: M): M

  //////////////////////////////////////////
  // A o scalar
  //////////////////////////////////////////

  def +(that: Double): M

  def -(that: Double): M

  def *(that: Double): M

  def /(that: Double): M

  //////////////////////////////////////////
  // columnwise M o vector (broadcast operators)
  //////////////////////////////////////////

//  def broadcastRows(mat: A, vec:  A, op: (Double, Double) => Double): A
//
//  def broadcastCols(mat: A, vec:  A, op: (Double, Double) => Double): A
//
//  def broadcast(mat: A, vec:  A)(op: (Double, Double) => Double): A

  def +(that:  V): M// = broadcast(this, that)(_ + _)

  def -(that:  V): M// = broadcast(this, that)(_ - _)

  def *(that:  V): M// = broadcast(this, that)(_ * _)

  def /(that:  V): M// = broadcast(this, that)(_ / _)

  //////////////////////////////////////////
  // cellwise A o A
  //////////////////////////////////////////

  def +(that: M): M

  def -(that: M): M

  def *(that: M): M

  def /(that: M): M

  //////////////////////////////////////////
  // A x A -> A and  A x A -> A
  //////////////////////////////////////////

  def %*%(that: M): M

  def %*%(that: V): V

  //////////////////////////////////////////
  // A operation
  //////////////////////////////////////////

  def t: M

  def ^(n: Int): M

  /**
    * Reshapes the [[MatrixOps]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows number of rows of the new A
    * @param cols number of columns of the new A
    * @param byRow if true, Ais reshaped my row
    * @return new Awith the new dimensions and rearranged values
    */
  def reshape(rows: Int, cols: Int, byRow: Boolean = true): M

  def copy: M
}

object MatrixOps {

  implicit object LazyMatrixOps extends MatrixOps[LazyVector, LazyMatrix] {
    def +(mat: LazyMatrix, vec: LazyVector): LazyMatrix = ???
  }

  def apply[V:Vector, M:MatrixOps]: MatrixOps[V, M] = implicitly

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
  def apply(impl: Array[Double], rows: Int, cols: Int): LazyMatrix = ???

  def apply(values: Seq[Double], rows: Int, cols: Int): LazyMatrix = apply(values.toArray, rows, cols)

  def fromDataFrame(df: DataFrame): LazyMatrix = ???

//  private[sysml] def fill(rows: Int, cols: Int)(gen: (Int, Int) => Double): A= {
//    require(rows * cols < Int.AaxAalue)
//    val array = new Array[Double](rows * cols)
//    for (i <- 0 until rows; j <- 0 until cols) {
//      array((i * cols) + j) = gen(i, j)
//    }
//    new A(array, rows, cols)
//  }

  def zeros(rows: Int, cols: Int): LazyMatrix = new LazyMatrix(Application("matrix", List("0", s"$rows", s"$cols"))) // A.fill(rows, cols)((i, j) => 0.0)

  // AODO: support more parameters (min, max, distribution, sparsity, seed)
  def rand(rows: Int, cols: Int): LazyMatrix = ??? //T.fill(rows, cols)((i, j) => Random.nextDouble())

  /** generate Awith the vector on the diagonal */

}

