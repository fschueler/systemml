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

package org.apache.sysml.compiler.macros

import org.apache.spark
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql._
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.sysml.api.linalg.Matrix
import org.apache.sysml.api.linalg.api._
import org.apache.sysml.api.mlcontext.{MLContext, Matrix => MLMatrix}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.util.Random

/** A spec for SystemML Algorithms. */
@RunWith(classOf[JUnitRunner])
class RewriteMacrosSpec extends FreeSpec with Matchers {
  val conf = new SparkConf()
    .setMaster("local[2]")
    .setAppName("SystemML Spark App")

  lazy val sc: SparkContext = new SparkContext(conf)
  lazy val spark = SparkSession.builder().getOrCreate()

  implicit lazy val mlctx: MLContext = new MLContext(sc)

  "Matrix output" in {

    val alg = parallelize {
      val m = Matrix(Seq(11.0, 22.0, 33.0, 44.0), 2, 2)
      val n = sum(m)
      (m, n)
    }

    val (m: Matrix, n: Double) = alg.run()
  }

  "Matrix Multiplication" in {

    val alg = parallelize {
      val A = Matrix.rand(5, 3)
      val B = Matrix.rand(3, 7)
      val C = A %*% B
      C
    }

    val res = alg.run()
  }

  "For loop" in {

    val loop = parallelize {
      val A = Matrix.rand(3, 3)
      val B = Matrix.rand(3, 3)
      var C = Matrix.zeros(3, 3)

      val iter = 10
      var s = 0.0

      for (i <- 1 to iter) {
        C = A %*% B
        s = sum(C)
        println(s)
      }
    }

    val res = loop.run()
  }

  "NMF" in {

    val nmf: SystemMLAlgorithm[(Matrix, Matrix)] = parallelize {
      val tfidf = Array(1.0, 2.0, 3.0, 4.0) // tfidf feature matrix coming from somewhere
      val k = 40
      val m, n = 2 // dimensions of tfidf
      val maxIters = 200

      val V = Matrix(tfidf, m, n) // initialize matrices
      var W = Matrix.rand(m, k)
      var H = Matrix.rand(k, n)

      for (i <- 0 to maxIters) { //main loop
        H = H * (W.t %*% V) / (W.t %*% (W %*% H))
        W = W * (V %*% H.t) / (W %*% (H %*% H.t))
      }

      (W, H) // return values
    }

    val (w, h) = nmf.run()
    println(w)
  }


  "MinMaxMean from DataFrame" in {
    val numRows = 1000
    val numCols = 1000
    val data = sc.parallelize(0 to numRows-1).map { _ => Row.fromSeq(Seq.fill(numCols)(Random.nextDouble)) }
    val schema = StructType((0 to numCols-1).map { i => StructField("C" + i, DoubleType, true) } )
    val df = spark.createDataFrame(data, schema)

    val alg = parallelize {
      /* this should take a dataframeand set it as input to the MLContext */
      val matrix: Matrix = Matrix.fromDataFrame(df) // can we find out the metadata?

      val minOut = min(matrix)
      val maxOut = max(matrix)
      val meanOut = mean(matrix)

      (minOut, maxOut, meanOut)
    }

    val  (minOut: Double, maxOut: Double, meanOut: Double) = alg.run()

    println(s"The minimum is $minOut, maximum: $maxOut, mean: $meanOut")

  }

  "UDF support" in {

    val alg = parallelize {
      def myAdd(x: Double, y: Double): Double = {
        x + y
      }

      val d = myAdd(1.0, 1.0)
      val e = myAdd(d, d)
      (d, e)
    }

    val res: (Double, Double) = alg.run()
    println(res)
  }

  "While Loop" in {

    val alg = parallelize {
      var x = 5
      var y = 100

      while (x > 0) {
        x = x - 1
        y = y / 2
      }

      (x, y)
    }

    val res = alg.run()
    println(res)
  }

  "Read csv" in {

    val alg = parallelize {
      // Read the input data
      val data: Matrix = read("/home/felix/repos/incubator-systemml/src/test/resources/iris.data", Format.CSV)
      val c = data.ncol
      val r = data.nrow

      println("r: " + r + ", c: " + c)

      // initialize result vector
      var stats = Matrix.rand(1, c)

      for (i <- 1 until 2) {
        stats = stats + 5.0
      }

      stats = stats / r

      (stats, r, c)
    }

    val res = alg.run()
    println(res)
  }
}
