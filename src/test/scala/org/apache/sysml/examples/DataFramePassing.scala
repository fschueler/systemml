package org.apache.sysml.examples

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.sysml.api.linalg.Matrix
import org.apache.sysml.api.linalg.api.{max, mean, min, parallelize}
import org.apache.sysml.api.mlcontext.MLContext

import scala.util.Random

object DataFramePassing extends App {
  val numRows = 100
  val numCols = 100
  val spark = SparkSession.builder().master("local[2]").appName("DataFramePassing").getOrCreate()
  val sc = spark.sparkContext
  val data = sc.parallelize(0 to numRows-1).map { _ => Row.fromSeq(Seq.fill(numCols)(Random.nextDouble)) }
  val schema = StructType((0 to numCols-1).map { i => StructField("C" + i, DoubleType, true) } )
  val df = spark.createDataFrame(data, schema)

  implicit val mlctx = new MLContext(sc)

  def plusOne: Double => Double = x => x + 1.0

  val x = 5.0

  val alg = parallelize {
    /* this should take a dataframeand set it as input to the MLContext */
    val matrix: Matrix = Matrix.fromDataFrame(df) // can we find out the metadata?

    val tr = matrix.t

    val y = plusOne(x)

    val minOut = x
    val maxOut = max(matrix)
    val meanOut = mean(matrix)

    (minOut, maxOut, meanOut, tr)
  }

  val  (minOut: Double, maxOut: Double, meanOut: Double, tr: Matrix) = alg.run()

  println(s"The minimum is $minOut, maximum: $maxOut, mean: $meanOut")

  val alg2 = parallelize {
    val M = Matrix.rand(3, 3)
    val N = M %*% tr

    N
  }

  val res = alg2.run()

}
