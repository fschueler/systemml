package org.apache.sysml.api.linalg.types

import org.apache.spark.SparkContext
import org.apache.sysml.api.linalg.Matrix

import TypeClass._

object APITest extends App {

    val A = Matrix(Array(1.0, 1.0, 1.0))

    val C = A + A

    C

}
