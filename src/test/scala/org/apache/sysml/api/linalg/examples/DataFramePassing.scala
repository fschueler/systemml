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

package org.apache.sysml.api.linalg.examples

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.sysml.api.linalg.Matrix
import org.apache.sysml.api.linalg.api.{max, mean, systemml}
import org.apache.sysml.api.mlcontext.MLContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.Random

@RunWith(classOf[JUnitRunner])
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

  val alg = systemml {
    /* this should take a dataframeand set it as input to the MLContext */
    val matrix: Matrix = Matrix.fromDataFrame(df) // can we find out the metadata?

    val tr = matrix.t

    // val y = plusOne(x)

    val minOut = x
    val maxOut = max(matrix)
    val meanOut = mean(matrix)

    (minOut, maxOut, meanOut, tr)
  }

  val  (minOut: Double, maxOut: Double, meanOut: Double, tr: Matrix) = alg.run(mlctx, true)

  println(s"The minimum is $minOut, maximum: $maxOut, mean: $meanOut")

  val alg2 = systemml {
    val M = Matrix.rand(100, 100)
    val N = M %*% tr

    N
  }

  val res = alg2.run(mlctx, true)

}
