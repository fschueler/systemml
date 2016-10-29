package org.apache.sysml.api.linalg.types

import org.apache.spark.SparkContext
import org.apache.sysml.api.linalg.Matrix

object APITest extends App {

  val sc = new SparkContext()

    val A = Matrix.zeros(3, 3)
    val B = Matrix.rand(3, 3)

    val C = A + B

    val res = C.eval(sc)
    res

}
