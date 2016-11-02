package org.apache.sysml.api.linalg

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.sysml.api.linalg.types.TypeClass.Layout
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

  def read[A: Layout](path: String): Matrix[A] = ???

  def write[A: Layout](mat: Matrix[A], path: String, format: Format.FileFormat): Unit = ???

  def sum[A: Layout](mat: Matrix[A]): Double = ???

  def rowSums[A: Layout](mat: Matrix[A]): A = ???

  def colSums[A: Layout](mat: Matrix[A]): Matrix[A] = ???

  def mean[A: Layout](mat: Matrix[A]): Double = ???

  def rowMeans[A: Layout](mat: Matrix[A]): Matrix[A] = ???

  def colMeans[A: Layout](mat: Matrix[A]): Matrix[A] = ???

  def log[A: Layout](x: Double): Double = ???

  def log[A: Layout](mat: Matrix[A]): Matrix[A] = ???

  def abs(x: Double): Double = ???

  def exp[A: Layout](b: Matrix[A]): Matrix[A] = ???

  def rowIndexMax[A: Layout](mat: Matrix[A]): Matrix[A] = ???

  def pmax[A: Layout](mat: Matrix[A], s: Double): Matrix[A] = ???

  def min[A: Layout](mat: Matrix[A]): Double = ???

  def max[A: Layout](Mat: Matrix[A]): Double = ???

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
    def +[A: Layout](v: Matrix[A]): Matrix[A] = v + n

    def -[A: Layout](v: Matrix[A]): Matrix[A] = v - n

    def *[A: Layout](v: Matrix[A]): Matrix[A] = v * n

    def /[A: Layout](v: Matrix[A]): Matrix[A] = v / n
  }

  object Format {
    sealed trait FileFormat
    case object CSV extends FileFormat
    case object BINARY extends FileFormat
  }
}
