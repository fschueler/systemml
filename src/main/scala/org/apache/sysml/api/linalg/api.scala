package org.apache.sysml.api.linalg

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
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

  def read(path: String): Matrix[_, _] = ???

  def write(mat: Matrix[_, _], path: String, format: Format.FileFormat): Unit = ???

  def sum[V, M](mat: Matrix[V, M]): Double = ???

  def rowSums[A, B](mat: Matrix[A, B]): A = ???

  def colSums(mat: Matrix[_, _]): Matrix[_, _] = ???

  def mean(mat: Matrix[_, _]): Double = ???

  def rowMeans(mat: Matrix[_, _]): Matrix[_, _] = ???

  def colMeans(mat: Matrix[_, _]): Matrix[_, _] = ???

  def log(x: Double): Double = ???

  def log(mat: Matrix[_, _]): Matrix[_, _] = ???

  def abs(x: Double): Double = ???

  def exp(b: Matrix[_, _]): Matrix[_, _] = ???

  def rowIndexMax(mat: Matrix[_, _]): Matrix[_, _] = ???

  def pmax(mat: Matrix[_, _], s: Double): Matrix[_, _] = ???

  def min(mat: Matrix[_, _]): Double = ???

  def max(Mat: Matrix[_, _]): Double = ???

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
    def +[B <: Matrix[_, B]](v: B): B = v + n

    def +[A <: Matrix[A, _]](v: A): A = v + n

    def -[A, B <: Matrix[A, B]](v: Matrix[A, B]): Matrix[A, B] = v - n

    def *[A, B <: Matrix[A, B]](v: Matrix[A, B]): Matrix[A, B] = v * n

    def /[A, B <: Matrix[A, B]](v: Matrix[A, B]): Matrix[A, B] = v / n
  }

  object Format {
    sealed trait FileFormat
    case object CSV extends FileFormat
    case object BINARY extends FileFormat
  }
}
