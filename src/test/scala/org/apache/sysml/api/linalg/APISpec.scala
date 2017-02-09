package org.apache.sysml.api.linalg

import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.sysml.api.BaseAPISpec
import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api._
import org.apache.sysml.api.mlcontext.MLContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class APISpec extends BaseAPISpec {
  val spark = SparkSession.builder().appName("RewriteMacroSpec").master("local[4]").getOrCreate()
  val sc    = spark.sparkContext

  implicit var mlctx: MLContext = _

  "Constructors" - {

    "Matrix" - {
      "ones, zeros, rand, diag" in {
        mlctx = new MLContext(sc)

        val algorithm = parallelize {
          val A = Matrix.ones(2, 2)
          val B = Matrix.zeros(2, 2)
          val C = Matrix.rand(2, 2)
          val D = Matrix.diag(1.0, 2)

          (A, B, C, D)
        }

        algorithm.inputs shouldBe empty
        algorithm.outputs shouldEqual Seq("A", "B", "C", "D")

        val result = algorithm.run()

        result._1 shouldEqual Matrix(Seq(1.0, 1.0, 1.0, 1.0), 2, 2)
        result._2 shouldEqual Matrix(Seq(0.0, 0.0, 0.0, 0.0), 2, 2)

        result._4 shouldEqual Matrix(Seq(1.0, 0.0, 0.0, 1.0), 2, 2)
      }

      "fromDataFrame" in {
        mlctx = new MLContext(sc)

        object dfTest extends Serializable {

          val numRows = 10
          val numCols = 7

          val data = sc.parallelize(0 to numRows - 1).map { _ => Row.fromSeq(Seq.fill(numCols)(Random.nextDouble)) }
          val schema = StructType((0 to numCols - 1).map { i => StructField("C" + i, DoubleType, true) })
          val df = spark.createDataFrame(data, schema)

          val algorithm = parallelize {
            val A = Matrix.fromDataFrame(df)
            val B = Matrix.fromDataFrame(df)

            (A, B)
          }
        }

        dfTest.algorithm.inputs.length shouldBe 1

        val dfName = dfTest.algorithm.inputs.headOption match {
          case Some((name, _)) => name
        }
        dfName shouldEqual "df"

        dfTest.algorithm.outputs shouldEqual Seq("A", "B")

        val result = dfTest.algorithm.run()

        // TODO check result
      }

      "apply" in {
        mlctx = new MLContext(sc)

        val algorithm = parallelize {
          val A = Matrix(Seq(1.0, 2.0, 3.0, 4.0), 2, 2)
          val B = Matrix(Array(1.0, 2.0, 3.0, 4.0), 2, 2)

          (A, B)
        }

        algorithm.inputs shouldBe empty
        algorithm.outputs shouldEqual Seq("A", "B")

        val result = algorithm.run()

        result shouldEqual(Matrix(Seq(1.0, 2.0, 3.0, 4.0), 2, 2),
                           Matrix(Seq(1.0, 2.0, 3.0, 4.0), 2, 2))
      }

      "reshape" in {
        mlctx = new MLContext(sc)

        val algorithm = parallelize {
          val A = Matrix(Seq(1.0, 2.0, 3.0, 4.0), 2, 2)
          val B = Matrix.reshape(A, 4, 1)
          val C = Matrix.reshape(B, 2, 2)

          (A, B, C)
        }

        algorithm.inputs shouldBe empty
        algorithm.outputs shouldEqual Seq("A", "B", "C")

        val result = algorithm.run()

        result shouldEqual(Matrix(Seq(1.0, 2.0, 3.0, 4.0), 2, 2),
                           Matrix(Seq(1.0, 2.0, 3.0, 4.0), 4, 1),
                           Matrix(Seq(1.0, 2.0, 3.0, 4.0), 2, 2))
      }

      "indexing" in {
        mlctx = new MLContext(sc)

        val algorithm = parallelize {
          val A = Matrix.ones(3, 3)
          val B = Matrix.ones(3, 3)
          val C = Matrix.zeros(3, 3)
          val D = Matrix.zeros(3, 3)

          val a = A(0, 0) // (idx, idx)
          val b = B(0, :::)  // (idx, :::)
          val c = C(:::, 0) // (:::, idx)
          val d = D(:::, 0 to 1) // (:::, range)
          val e = A(0 to 1, :::) // (range, :::)
          val f = B(0, 0 to 1) // (idx, range)
          val g = C(0 to 1, 0) // (range, idx)
          val h = D(0 to 1, 0 to 1) // (range, range)

          (a, b, c, d, e, f, g, h)
        }

        algorithm.inputs shouldBe empty
        algorithm.outputs shouldEqual Seq("a", "b", "c", "d", "e", "f", "g", "h")

        val result = algorithm.run()

        result shouldEqual(1.0,
                           Matrix(Seq(1.0, 1.0, 1.0), 1, 3),
                           Matrix(Seq(0.0, 0.0, 0.0), 3, 1),
                           Matrix(Seq(0.0, 0.0, 0.0, 0.0, 0.0, 0.0), 3, 2),
                           Matrix(Seq(1.0, 1.0, 1.0, 1.0, 1.0, 1.0), 2, 3),
                           Matrix(Seq(1.0, 1.0), 1, 2),
                           Matrix(Seq(0.0, 0.0), 2, 1),
                           Matrix(Seq(0.0, 0.0, 0.0, 0.0), 2, 2))
      }

      "updating" in {

      }
    }

    "Vector" - {
      "apply" in {
        mlctx = new MLContext(sc)

        val algorithm = parallelize {
          val v = Vector.apply(Array(1.0, 2.0, 3.0, 4.0))
          val w = Vector.apply(Array(1.0, 2.0, 3.0, 4.0))

          (v, w)
        }

        algorithm.inputs shouldBe empty
        algorithm.outputs shouldEqual Seq("v", "w")

        val result = algorithm.run()

        result shouldEqual(Matrix(Array(1.0, 2.0, 3.0, 4.0), 4, 1),
                           Matrix(Array(1.0, 2.0, 3.0, 4.0), 4, 1))
      }

      "rand, ones, zeros" in {
        mlctx = new MLContext(sc)

        val algorithm = parallelize {
          val v = Vector.rand(4)
          val w = Vector.ones(4)
          val x = Vector.zeros(4)

          (v, w, x)
        }

        algorithm.inputs shouldBe empty
        algorithm.outputs shouldEqual Seq("v", "w", "x")

        val result = algorithm.run()

        result._2 shouldEqual Matrix(Array(1.0, 1.0, 1.0, 1.0), 4, 1)
        result._3 shouldEqual Matrix(Array(0.0, 0.0, 0.0, 0.0), 4, 1)
      }
    }
  }

  "Unary Operations" - {

    "Matrix" - {

      ".t, .ncol, .nrow" in {
        mlctx = new MLContext(sc)

        val algorithm = parallelize {
          val A = Matrix(Seq(1.0, 2.0, 3.0, 4.0), 2, 2)

          val B = A.t
          val C = A.nrow
          val D = A.ncol

          (B, C, D)
        }

        algorithm.inputs shouldBe empty
        algorithm.outputs shouldEqual Seq("B", "C", "D")

        val result = algorithm.run()

        result shouldEqual(Matrix(Seq(1.0, 3.0, 2.0, 4.0), 2, 2), 2, 2)
      }
    }
  }

  "Binary Operations" - {
    "Scalar - Scalar" - {

      "+, -, *, /" - {

        "Double - Double" in {
          mlctx = new MLContext(sc)

          val algorithm = parallelize {
            val a = 5.0
            val b = 2.0

            val c = a + b
            val d = a - b
            val e = a * b
            val f = a / b

            (c, d, e, f)
          }

          algorithm.inputs shouldBe empty
          algorithm.outputs shouldEqual Seq("c", "d", "e", "f")

          val result = algorithm.run()

          result shouldEqual(7.0, 3.0, 10.0, 2.5)
        }

        "Int - Int" in {
          mlctx = new MLContext(sc)

          val algorithm = parallelize {
            val a = 5
            val b = 2

            val c = a + b
            val d = a - b
            val e = a * b
            val f = a / b

            (c, d, e, f)
          }

          algorithm.inputs shouldBe empty
          algorithm.outputs shouldEqual Seq("c", "d", "e", "f")

          val result = algorithm.run()

          result shouldEqual(7, 3, 10, 2.5)
        }

        "Double - Int" in {
          mlctx = new MLContext(sc)

          val algorithm = parallelize {
            val a = 5.0
            val b = 2

            val c = a + b
            val d = a - b
            val e = a * b
            val f = a / b

            (c, d, e, f)
          }

          algorithm.inputs shouldBe empty
          algorithm.outputs shouldEqual Seq("c", "d", "e", "f")

          val result = algorithm.run()

          result shouldEqual(7.0, 3.0, 10.0, 2.5)
        }

        "Int - Double" in {
          mlctx = new MLContext(sc)

          val algorithm = parallelize {
            val a = 5
            val b = 2.0

            val c = a + b
            val d = a - b
            val e = a * b
            val f = a / b

            (c, d, e, f)
          }

          algorithm.inputs shouldBe empty
          algorithm.outputs shouldEqual Seq("c", "d", "e", "f")

          val result = algorithm.run()

          result shouldEqual(7.0, 3.0, 10.0, 2.5)
        }
      }
    }

    "Matrix - Scalar" - {
      "+, -, *, /" - {
        "Matrix - Double" in {
          mlctx = new MLContext(sc)

          val algorithm = parallelize {
            val A = Matrix.ones(2, 2)
            val b = 5.0

            val C = A + b
            val D = A - b
            val E = A * b
            val F = A / b

            (C, D, E, F)
          }

          algorithm.inputs shouldBe empty
          algorithm.outputs shouldEqual Seq("C", "D", "E", "F")

          val result = algorithm.run()

          result shouldEqual(Matrix(Seq(6.0, 6.0, 6.0, 6.0), 2, 2),
            Matrix(Seq(-4.0, -4.0, -4.0, -4.0), 2, 2),
            Matrix(Seq(5.0, 5.0, 5.0, 5.0), 2, 2),
            Matrix(Seq(0.2, 0.2, 0.2, 0.2), 2, 2))
        }

        "Matrix - Int" in {
          mlctx = new MLContext(sc)

          val algorithm = parallelize {
            val A = Matrix.ones(2, 2)
            val b = 5

            val C = A + b
            val D = A - b
            val E = A * b
            val F = A / b

            (C, D, E, F)
          }

          algorithm.inputs shouldBe empty
          algorithm.outputs shouldEqual Seq("C", "D", "E", "F")

          val result = algorithm.run()

          result shouldEqual(Matrix(Seq(6.0, 6.0, 6.0, 6.0), 2, 2),
            Matrix(Seq(-4.0, -4.0, -4.0, -4.0), 2, 2),
            Matrix(Seq(5.0, 5.0, 5.0, 5.0), 2, 2),
            Matrix(Seq(0.2, 0.2, 0.2, 0.2), 2, 2))
        }

        "Double - Matrix" in {
          mlctx = new MLContext(sc)

          val algorithm = parallelize {
            val A = 5.0
            val b = Matrix.ones(2, 2)

            val C = A + b
            val D = A - b
            val E = A * b
            val F = A / b

            (C, D, E, F)
          }

          algorithm.inputs shouldBe empty
          algorithm.outputs shouldEqual Seq("C", "D", "E", "F")

          val result = algorithm.run()

          result shouldEqual(Matrix(Seq(6.0, 6.0, 6.0, 6.0), 2, 2),
            Matrix(Seq(4.0, 4.0, 4.0, 4.0), 2, 2),
            Matrix(Seq(5.0, 5.0, 5.0, 5.0), 2, 2),
            Matrix(Seq(5.0, 5.0, 5.0, 5.0), 2, 2))
        }

        "Int - Matrix" in {
          mlctx = new MLContext(sc)

          val algorithm = parallelize {
            val A = 5
            val b = Matrix.ones(2, 2)

            val C = A + b
            val D = A - b
            val E = A * b
            val F = A / b

            (C, D, E, F)
          }

          algorithm.inputs shouldBe empty
          algorithm.outputs shouldEqual Seq("C", "D", "E", "F")

          val result = algorithm.run()

          result shouldEqual(Matrix(Seq(6.0, 6.0, 6.0, 6.0), 2, 2),
            Matrix(Seq(4.0, 4.0, 4.0, 4.0), 2, 2),
            Matrix(Seq(5.0, 5.0, 5.0, 5.0), 2, 2),
            Matrix(Seq(5.0, 5.0, 5.0, 5.0), 2, 2))
        }
      }
    }

    "Matrix - Matrix" - {
      "+, -, *, /, %*%" in {
        mlctx = new MLContext(sc)

        val algorithm = parallelize {
          val A = Matrix.ones(2, 2)
          val B = Matrix.ones(2, 2)

          val C = A + B
          val D = A - B
          val E = A * B
          val F = A / B
          val G = A %*% B

          (C, D, E, F, G)
        }

        algorithm.inputs shouldBe empty
        algorithm.outputs shouldEqual Seq("C", "D", "E", "F", "G")

        val result = algorithm.run()

        result shouldEqual(Matrix(Seq(2.0, 2.0, 2.0, 2.0), 2, 2),
          Matrix(Seq(0.0, 0.0, 0.0, 0.0), 2, 2),
          Matrix(Seq(1.0, 1.0, 1.0, 1.0), 2, 2),
          Matrix(Seq(1.0, 1.0, 1.0, 1.0), 2, 2),
          Matrix(Seq(2.0, 2.0, 2.0, 2.0), 2, 2))
      }
    }
  }

  "Builtin functions" - {

  }
}
