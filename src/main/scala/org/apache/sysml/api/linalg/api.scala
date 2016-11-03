package org.apache.sysml.api.linalg

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.sysml.api.linalg.types.TypeClass.{Block, DenseBlock, LazyEval, Local, Strategy}
import org.apache.sysml.api.mlcontext.MLContext
import org.apache.sysml.compiler.macros.RewriteMacros

import scala.language.experimental.macros

package object api {

  private val conf = new SparkConf()
    .setMaster("local[2]")
    .setAppName("SystemML Spark App")

  lazy val sc: SparkContext = new SparkContext(conf)
  lazy val sqlContext: SQLContext = new SQLContext(sc)

  implicit lazy val mlctx: MLContext = new MLContext(sc)

  /**
    * The entry point for the systemML macro
    */
  trait SystemMLAlgorithm[T] {
    def run(): T
  }

  final def parallelize[T](e: T): SystemMLAlgorithm[T] = macro RewriteMacros.impl[T]

  object :::

  def read[A: Strategy](path: String): Matrix[A] = ???

  def write[A: Strategy](mat: Matrix[A], path: String, format: Format.FileFormat): Unit = ???

  def sum[A: Strategy](mat: Matrix[A]): Double = ???

  def rowSums[A: Strategy](mat: Matrix[A]): A = ???

  def colSums[A: Strategy](mat: Matrix[A]): Matrix[A] = ???

  def mean[A: Strategy](mat: Matrix[A]): Double = ???

  def rowMeans[A: Strategy](mat: Matrix[A]): Matrix[A] = ???

  def colMeans[A: Strategy](mat: Matrix[A]): Matrix[A] = ???

  def log[A: Strategy](x: Double): Double = ???

  def log[A: Strategy](mat: Matrix[A]): Matrix[A] = ???

  def abs(x: Double): Double = ???

  def exp[A: Strategy](b: Matrix[A]): Matrix[A] = ???

  def rowIndexMax[A: Strategy](mat: Matrix[A]): Matrix[A] = ???

  def pmax[A: Strategy](mat: Matrix[A], s: Double): Matrix[A] = ???

  def min[A: Strategy](mat: Matrix[A]): Double = ???

  def max[A: Strategy](Mat: Matrix[A]): Double = ???

  ///////////////////////////////////
  // Implicit Matrix and Vector Ops
  ///////////////////////////////////

  /** This allows operations with Vectors and Matrices as left arguments such as Double * Matrix */
//  implicit class VectorOps(private val n: Double) extends AnyVal {
//    def +(v: Vector): Vector = v + n
//
//    def -(v: Vector): Vector = v - n
//
//    def *(v: Vector): Vector = v * n
//
//    def /(v: Vector): Vector = v / n
//  }

  implicit class MatrixOps(private val n: Double) extends AnyVal{
    def +[A: Strategy](v: Matrix[A]): Matrix[A] = v + n

    def -[A: Strategy](v: Matrix[A]): Matrix[A] = v - n

    def *[A: Strategy](v: Matrix[A]): Matrix[A] = v * n

    def /[A: Strategy](v: Matrix[A]): Matrix[A] = v / n
  }

  object Format {
    sealed trait FileFormat
    case object CSV extends FileFormat
    case object BINARY extends FileFormat
  }
}
