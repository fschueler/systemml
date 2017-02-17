package org.apache.sysml.compiler.macros

import java.io.File
import java.nio.file.Files

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.sysml.api.mlcontext.MLContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.tools.nsc.{GenericRunnerSettings, Settings}
import scala.tools.nsc.interpreter.IMain

@RunWith(classOf[JUnitRunner])
class ReplTest extends FreeSpec with Matchers {

  private def scalaOptionError(msg: String): Unit = {
    Console.err.println(msg)
  }

  val outputDir = Files.createTempDirectory("systemml_repl_test")
  val jars = "" ///home/felix/repos/incubator-systemml/target/SystemML.jar"

  val arguments = List(
    "-Xprint:parser",
    "-Yrepl-class-based",
    "-Yrepl-outdir", s"${outputDir}",
    "-classpath", jars
  )

  val settings = new GenericRunnerSettings(scalaOptionError)
  settings.processArguments(arguments, true)
  settings.usejavacp.value = true

  val repl = new IMain(settings)

  val imports =
    """
      |import org.apache.spark.SparkContext
      |import org.apache.spark.sql.SparkSession
      |import org.apache.sysml.api.mlcontext.MLContext
      |
      |import org.apache.sysml.api.linalg._
      |import org.apache.sysml.api.linalg.api._
      |
      |lazy val spark = SparkSession.builder().master("local[2]").appName("ReplTest").getOrCreate()
      |lazy val sc: SparkContext = spark.sparkContext
      |
      |implicit lazy val mlctx: MLContext = new MLContext(sc)
    """.stripMargin

  // imports
  repl.interpret(imports)

  "A macro should" - {

    "compile and run" in {
      val alg1 =
        """
          |val algorithm = systemml {
          |  val x = Matrix.rand(3, 3)
          |  val y = Matrix.rand(3, 3)
          |  val z = x + y
          |  z
          |}
        """.stripMargin

      // call macro and create algorithm instance
      repl.interpret(alg1)

      val run1 =
        """
          |val res = algorithm.run()
        """.stripMargin

      repl.interpret(run1)
    }
  }

  "A Dataframe passed to the macro" - {

    "Should be converted to a matrix" in {
      val crDF =
        """
          |val df = spark.read.format("com.databricks.spark.csv").option("header", "true").load("/data/arxiv_abstracts/cs_abstracts.csv")
        """.stripMargin

      // create a dataframe
      repl.interpret(crDF)

      val alg1 =
        """
          |val algorithm = systemml {
          |  val x = Matrix.fromDataFrame(df)
          |  val y = sum(x)
          |  y
          |}
        """.stripMargin

      // call macro and create algorithm instance
      repl.interpret(alg1)

      val run1 =
        """
          |val res = algorithm.run()
        """.stripMargin

      repl.interpret(run1)

    }
  }

}
