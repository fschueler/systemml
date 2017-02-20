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

package org.apache.sysml.examples

import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api._
import org.apache.sysml.api.mlcontext.MLContext

import org.apache.spark.sql.functions.udf

object NMF extends App {

    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("SystemML Spark App")

    val sc: SparkContext = new SparkContext(conf)
    val spark = SparkSession.builder().getOrCreate()

  // read data
  val df = spark.read.format("com.databricks.spark.csv").option("header", "true").load("/data/arxiv_abstracts/cs_abstracts.csv")
  df.show(10)

  // todo add document index (zipwithindex)

  // combine titles and abstracts
  def combine = udf((x: String, y: String) => (x, y) match {
    case (x: String, y: String) => x ++ y
    case (x: String, null) => x
    case (null, y: String) => y
  })

  val dfTransformed = df.withColumn("combined", combine.apply(df("title"), df("abstract")))
  dfTransformed.show(10)

  // tokenize
  val tokenizer = new Tokenizer().setInputCol("combined").setOutputCol("words")
  val wordsData = tokenizer.transform(dfTransformed)
  wordsData.show(10)

  // hashing transformer to get term frequency
  val hashingTF = new HashingTF()
    .setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(100)

  val featurizedData = hashingTF.transform(wordsData)
  featurizedData.show(10)

  // compute inverse document frequency
  val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
  val idfModel = idf.fit(featurizedData)
  val rescaledData = idfModel.transform(featurizedData)
  rescaledData.show(10)

  // combine titles and abstracts
  def project = udf((x: SparseVector) => x match {
    case y: SparseVector => y.toDense
  })

  //TODO convert to RDD in (i j v)-format

  val tfidf = rescaledData.withColumn("dense_features", project.apply(rescaledData("features"))).select("dense_features")
  tfidf.show(10, truncate = false)

  implicit val mlctx: MLContext = new MLContext(sc)

  val nmf = systemml {
    val V = Matrix.fromDataFrame(tfidf) // tfidf feature matrix coming from somewhere
    val k = 40
    val m, n = 100 // dimensions of tfidf
    val maxIters = 200

    var W = Matrix.rand(m, k)
    var H = Matrix.rand(k, n)

    for (i <- 0 to maxIters) { //main loop
      H = H * (W.t %*% V) / (W.t %*% (W %*% H))
      W = W * (V %*% H.t) / (W %*% (H %*% H.t))
    }

    (W, H) // return values
  }

  val (w, h) = nmf.run(false)
}
