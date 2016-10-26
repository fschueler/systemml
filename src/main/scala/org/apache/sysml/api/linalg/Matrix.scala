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

import scala.util.Random

// TODO: sparsity
// TODO: make T generic? what are possible value types?

/**
  * T class for SystemML
  *
  * Represents the T that will be translated to SystemML's T type.
  * This uses f-bounded type polymorphism (https://twitter.github.io/scala_school/advanced-types.html#fbounded)
  */
abstract class Matrix[T <: Matrix[T]] {
  val rows: Int
  val cols: Int

  def dims: (Int, Int) = (rows, cols)

  //////////////////////////////////////////
  // Constructors
  //////////////////////////////////////////


  //////////////////////////////////////////
  // Accessors
  //////////////////////////////////////////

  def apply(row: Int, col: Int): Double

  def apply(row: Int, col: :::.type ): Vector

  def apply(row: :::.type, col: Int): Vector

  def apply(rows: Range.Inclusive, cols: :::.type): T

  def apply(rows: :::.type, cols: Range.Inclusive): T

  def apply(rows: Range.Inclusive, cols: Range.Inclusive): T

  //////////////////////////////////////////
  // Left Indexing assignments
  //////////////////////////////////////////

  // TODO make sure that the orientation of the vector (row/col) fits the assignment
  def update(row: Int, col: Int, value: Double): T

  def update(row: Int, col: :::.type, vec: Vector): T

  def update(row: :::.type, col: Int, vec: Vector): T

  def update(rows: Range.Inclusive, cols: :::.type, mat: T): T

  def update(rows: :::.type, cols: Range.Inclusive, mat: T): T

  def update(rows: Range.Inclusive, cols: Range.Inclusive, mat: T): T

  //////////////////////////////////////////
  // M o scalar
  //////////////////////////////////////////

  def +(that: Double): T

  def -(that: Double): T

  def *(that: Double): T

  def /(that: Double): T

  //////////////////////////////////////////
  // columnwise M o vector (broadcast operators)
  //////////////////////////////////////////

//  def broadcastRows(mat: T, vec: Vector, op: (Double, Double) => Double): T
//
//  def broadcastCols(mat: T, vec: Vector, op: (Double, Double) => Double): T
//
//  def broadcast(mat: T, vec: Vector)(op: (Double, Double) => Double): T

  def +(that: Vector): T // = broadcast(this, that)(_ + _)

  def -(that: Vector): T // = broadcast(this, that)(_ - _)

  def *(that: Vector): T // = broadcast(this, that)(_ * _)

  def /(that: Vector): T // = broadcast(this, that)(_ / _)

  //////////////////////////////////////////
  // cellwise M o M
  //////////////////////////////////////////

  def +(that: T): T

  def -(that: T): T

  def *(that: T): T

  def /(that: T): T

  //////////////////////////////////////////
  // M x M -> M and  M x V -> V
  //////////////////////////////////////////

  def %*%(that: T): T

  def %*%(that: Vector): Vector

  //////////////////////////////////////////
  // M operation
  //////////////////////////////////////////

  def t: T

  def ^(n: Int): T

  def map(f: Double => Double): T

  // TODO: Should this return Either[Vector, T] depending on if one dimension is 1?
  /**
    * Reshapes the [[T]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows number of rows of the new T
    * @param cols number of columns of the new T
    * @param byRow if true, T is reshaped my row
    * @return new T with the new dimensions and rearranged values
    */
  def reshape(rows: Int, cols: Int, byRow: Boolean = true): T

  def copy: T
}

object Matrix {

  /**
    * This should be the primary way of constructing a [[Matrix]] from a sequence of values.
    * The [[Matrix]] is constructed column-major order, i.e. the [[Array]] (1, 2, 1, 2) with dimensions (2,2) will
    * generate the [[Matrix]]
    *   1 1
    *   2 2
    * @param impl the values that will be assignmed to the cells of the T in column-major order
    * @param rows number of rows of the generated T
    * @param cols number of columns of the generated T
    * @return a [[Matrix]] with values as cell entries and dimensionality (rows, cols)
    */
  def apply(impl: Array[Double], rows: Int, cols: Int): Matrix[_] = ???

  def apply(values: Seq[Double], rows: Int, cols: Int): Matrix[_] = apply(values.toArray, rows, cols)

  def fromDataFrame(df: DataFrame): Matrix[_] = ???

//  private[sysml] def fill(rows: Int, cols: Int)(gen: (Int, Int) => Double): T = {
//    require(rows * cols < Int.MaxValue)
//    val array = new Array[Double](rows * cols)
//    for (i <- 0 until rows; j <- 0 until cols) {
//      array((i * cols) + j) = gen(i, j)
//    }
//    new T(array, rows, cols)
//  }

  def zeros(rows: Int, cols: Int): Matrix[_] = new EagerMatrix() // T.fill(rows, cols)((i, j) => 0.0)

  // TODO: support more parameters (min, max, distribution, sparsity, seed)
  def rand(rows: Int, cols: Int): Matrix[_] = ??? //T.fill(rows, cols)((i, j) => Random.nextDouble())

  /** generate T with the vector on the diagonal */
  def diag(vec: Vector): Matrix[_] = ??? //T.fill(vec.length, vec.length)((i, j) => if (i == j) vec(i) else 0.0)

}

