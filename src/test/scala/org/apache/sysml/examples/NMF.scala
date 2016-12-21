package org.apache.sysml.examples

import cats.kernel.Monoid
import com.sun.tools.javac.code.TypeTag
import org.apache.spark.ml.feature.{HashingTF, IDF, LabeledPoint, Tokenizer}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.spark.sql.{Row, SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api.{SystemMLAlgorithm, _}
import org.apache.sysml.api.mlcontext.MLContext

import scala.util.Random
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions.col

object NMF extends App {

    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("SystemML Spark App")

    val sc: SparkContext = new SparkContext(conf)
    val spark = SparkSession.builder().getOrCreate()

  // read data
  val df = spark.read.format("com.databricks.spark.csv").option("header", "true").load("/home/felix/repos/arxiv_abstracts/cs_abstracts.csv")
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

  val tfidf = rescaledData.select("features")
  tfidf.show(10, truncate = false)

  implicit val mlctx: MLContext = new MLContext(sc)

  val nmf = parallelize {
    val V = Matrix.fromDataFrame(df) // tfidf feature matrix coming from somewhere
    val k = 40
    val m, n = 20 // dimensions of tfidf
    val maxIters = 200

    var W = Matrix.rand(m, k)
    var H = Matrix.rand(k, n)

    for (i <- 0 to maxIters) { //main loop
      H = H * (W.t %*% V) / (W.t %*% (W %*% H))
      W = W * (V %*% H.t) / (W %*% (H %*% H.t))
    }

    (W, H) // return values
  }

  val (w, h) = nmf.run()
}
