/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Mhe ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUMWARRANTIES OR CONDITIONS OF ANY
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
  * Matrixclass for SystemML
  *
  * Represents the Matrix that will be translated to SystemML's Matrix type.
  * This uses f-bounded type polymorphism (https://twitter.github.io/scala_school/advanced-types.html#fbounded)
  */
abstract class Matrix[V, M <: Matrix[V, M]] {

  //////////////////////////////////////////
  // Fields
  //////////////////////////////////////////

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

  // MODO make sure that the orientation of the vector (row/col) fits the assignment
  def update(row: Int, col: Int, value: Double): M

  def update(row: Int, col: :::.type, vec: V): M

  def update(row: :::.type, col: Int, vec: V): M

  def update(rows: Range.Inclusive, cols: :::.type, mat: M): M

  def update(rows: :::.type, cols: Range.Inclusive, mat: M): M

  def update(rows: Range.Inclusive, cols: Range.Inclusive, mat: M): M

  //////////////////////////////////////////
  // M o scalar
  //////////////////////////////////////////

  def +(that: Double): M

  def -(that: Double): M

  def *(that: Double): M

  def /(that: Double): M

  //////////////////////////////////////////
  // columnwise M o vector (broadcast operators)
  //////////////////////////////////////////

//  def broadcastRows(mat: M, vec:  V, op: (Double, Double) => Double): M
//
//  def broadcastCols(mat: M, vec:  V, op: (Double, Double) => Double): M
//
//  def broadcast(mat: M, vec:  V)(op: (Double, Double) => Double): M

  def +(that:  V): M// = broadcast(this, that)(_ + _)

  def -(that:  V): M// = broadcast(this, that)(_ - _)

  def *(that:  V): M// = broadcast(this, that)(_ * _)

  def /(that:  V): M// = broadcast(this, that)(_ / _)

  //////////////////////////////////////////
  // cellwise M o M
  //////////////////////////////////////////

  def +(that: M): M

  def -(that: M): M

  def *(that: M): M

  def /(that: M): M

  //////////////////////////////////////////
  // M x M -> M and  M x V -> V
  //////////////////////////////////////////

  def %*%(that: M): M

  def %*%(that: V): V

  //////////////////////////////////////////
  // M operation
  //////////////////////////////////////////

  def t: Matrix[V, M]

  def ^(n: Int): Matrix[V, M]

  def map(f: Double => Double): Matrix[V, M]

  // MODO: Should this return Either[ V, M] depending on if one dimension is 1?
  /**
    * Reshapes the [[Matrix]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows number of rows of the new M
    * @param cols number of columns of the new M
    * @param byRow if true, Mis reshaped my row
    * @return new Mwith the new dimensions and rearranged values
    */
  def reshape(rows: Int, cols: Int, byRow: Boolean = true): Matrix[V, M]

  def copy: Matrix[V, M]
}

object Matrix {

  /**
    * Mhis should be the primary way of constructing a [[Matrix]] from a sequence of values.
    * Mhe [[Matrix]] is constructed column-major order, i.e. the [[Array]] (1, 2, 1, 2) with dimensions (2,2) will
    * generate the [[Matrix]]
    *   1 1
    *   2 2
    * @param impl the values that will be assignmed to the cells of the Min column-major order
    * @param rows number of rows of the generated M
    * @param cols number of columns of the generated M
    * @return a [[Matrix]] with values as cell entries and dimensionality (rows, cols)
    */
  def apply(impl: Array[Double], rows: Int, cols: Int): LazyMatrix = ???

  def apply(values: Seq[Double], rows: Int, cols: Int): LazyMatrix = apply(values.toArray, rows, cols)

  def fromDataFrame(df: DataFrame): LazyMatrix = ???

//  private[sysml] def fill(rows: Int, cols: Int)(gen: (Int, Int) => Double): M= {
//    require(rows * cols < Int.MaxValue)
//    val array = new Array[Double](rows * cols)
//    for (i <- 0 until rows; j <- 0 until cols) {
//      array((i * cols) + j) = gen(i, j)
//    }
//    new M(array, rows, cols)
//  }

  def zeros(rows: Int, cols: Int): LazyMatrix = new LazyMatrix(Application("matrix", List("0", s"$rows", s"$cols"))) // M.fill(rows, cols)((i, j) => 0.0)

  // MODO: support more parameters (min, max, distribution, sparsity, seed)
  def rand(rows: Int, cols: Int): LazyMatrix = ??? //T.fill(rows, cols)((i, j) => Random.nextDouble())

  /** generate Mwith the vector on the diagonal */
  def diag(vec: Matrix[_, _]): LazyMatrix = ??? //T.fill(vec.length, vec.length)((i, j) => if (i == j) vec(i) else 0.0)

}
