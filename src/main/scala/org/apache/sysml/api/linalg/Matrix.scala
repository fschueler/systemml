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
import org.apache.sysml.api.linalg.Distributions.Distribution
import org.apache.sysml.api.linalg.Lazy._
import org.apache.sysml.api.linalg.api.:::
import org.apache.sysml.api.linalg.types.TypeClass.{Block, DenseBlock, Strategy, LazyEval}

import scala.util.Random

/**
  * Matrixclass for SystemAL
  *
  * Represents the Matrix that will be translated to SystemAL's Matrix type.
  */
class Matrix[A: Strategy](val impl: A, override val rows: Long, override val cols: Long) extends MatrixOps[Vector[A], Matrix[A]] {

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

  override def apply(row: Int, col: Int): Double = ???

  override def apply(row: Int, col: :::.type ): Vector[A] = ???

  override def apply(row: :::.type, col: Int): Vector[A] = ???

  override def apply(rows: Range.Inclusive, cols: :::.type): Matrix[A] = ???

  override def apply(rows: :::.type, cols: Range.Inclusive): Matrix[A] = ???

  override def apply(rows: Range.Inclusive, cols: Range.Inclusive): Matrix[A] = ???

  //////////////////////////////////////////
  // Left Indexing assignments
  //////////////////////////////////////////

  // TODO make sure that the orientation of the vector (row/col) fits the assignment
  override def update(row: Int, col: Int, value: Double): Matrix[A] = ???

  override def update(row: Int, col: :::.type, vec: Vector[A]): Matrix[A] = ???

  override def update(row: :::.type, col: Int, vec: Vector[A]): Matrix[A] = ???

  override def update(rows: Range.Inclusive, cols: :::.type, mat: Matrix[A]): Matrix[A] = ???

  override def update(rows: :::.type, cols: Range.Inclusive, mat: Matrix[A]): Matrix[A] = ???

  override def update(rows: Range.Inclusive, cols: Range.Inclusive, mat: Matrix[A]): Matrix[A] = ???

  //////////////////////////////////////////
  // A o scalar
  //////////////////////////////////////////

  override def +(that: Double): Matrix[A] = new Matrix(this.impl + that, this.rows, this.cols)

  override def -(that: Double): Matrix[A] = ???

  override def *(that: Double): Matrix[A] = ???

  override def /(that: Double): Matrix[A] = ???

  //////////////////////////////////////////
  // columnwise M o vector (broadcast operators)
  //////////////////////////////////////////

//  def broadcastRows(mat: A, vec:  A, op: (Double, Double) => Double): A
//
//  def broadcastCols(mat: A, vec:  A, op: (Double, Double) => Double): A
//
//  def broadcast(mat: A, vec:  A)(op: (Double, Double) => Double): A

  override def +(that:  Vector[A]): Matrix[A] = ??? // = broadcast(this, that)(_ + _)

  override def -(that:  Vector[A]): Matrix[A] = ??? // = broadcast(this, that)(_ - _)

  override def *(that:  Vector[A]): Matrix[A] = ??? // = broadcast(this, that)(_ * _)

  override def /(that:  Vector[A]): Matrix[A] = ??? // = broadcast(this, that)(_ / _)

  //////////////////////////////////////////
  // cellwise A o A
  //////////////////////////////////////////

  override def +(that: Matrix[A]): Matrix[A] = {
    new Matrix(this.impl + that.impl, this.rows, this.cols)
  }

  override def -(that: Matrix[A]): Matrix[A] = ???

  override def *(that: Matrix[A]): Matrix[A] = ???

  override def /(that: Matrix[A]): Matrix[A] = ???

  //////////////////////////////////////////
  // A x A -> A and  A x A -> A
  //////////////////////////////////////////

  override def %*%(that: Matrix[A]): Matrix[A] = ???

  override def %*%(that: Vector[A]): Vector[A] = ???

  //////////////////////////////////////////
  // A operation
  //////////////////////////////////////////

  override def t: Matrix[A] = ???

  override def ^(n: Int): Matrix[A] = ???

  /**
    * Reshapes the [[Matrix]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows number of rows of the new A
    * @param cols number of columns of the new A
    * @param byRow if true, Ais reshaped my row
    * @return new Awith the new dimensions and rearranged values
    */
  override def reshape(rows: Int, cols: Int, byRow: Boolean = true): Matrix[A] = ???

  override def copy: Matrix[A] = ???

  def collect(): Matrix[EagerEval] = impl match {
    case ev: LazyEval => new Matrix(ev.collect(), rows, cols)
    case ev: EagerEval => new Matrix(ev, rows, cols)
  }

  override def toString(): String = {
    impl.toString()
  }
}

object Matrix {

  /**
    * Ahis should be the primary way of constructing a [[Matrix]] from a sequence of values.
    * Ahe [[Matrix]] is constructed column-major order, i.e. tMatrixhe [[Array]] (1, 2, 1, 2) with dimensions (2,2) will
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
    new Matrix(LazyEval(Application("matrix", List(Scalar(0), Scalar(rows), Scalar(cols)))), rows, cols)
  }

  def apply(values: Array[Double], rows: Int, cols: Int) = {
    new Matrix(LazyEval(Application("matrix", List(Application("seq", List(TSeq(values))), Scalar(rows), Scalar(cols)))), rows, cols)
  }

  // AODO: support more parameters (min, max, distribution, sparsity, seed)
  def rand(rows: Int, cols: Int, dist: Distribution, sparsity: Double) = {
    new Matrix(LazyEval(Application("rand", List(Scalar(rows), Scalar(cols), Literal(dist), Scalar(sparsity)))), rows, cols)
  }

  /** generate Awith the vector on the diagonal */

}

object Distributions {
  trait Distribution {
    def sample: Double
  }

  case class Normal() extends Distribution {
    override def sample: Double = ???
  }

  case class Poisson(lambda: Double) extends Distribution {
    override def sample: Double = ???
  }

  case class Uniform(a: Double, b: Double) extends Distribution {
    override def sample: Double = ???
  }
}

