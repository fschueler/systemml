package org.apache.sysml.api.linalg.types

import org.apache.spark.SparkContext
import org.apache.sysml.api.linalg.Matrix

import TypeClass._

object APITest extends App {

    val A = Matrix.rand(2, 2)

    val B = new Matrix(Local(DenseBlock(Array(1.0, 1.0, 1.0, 1.0))), 2, 2)

    val r = A + B

    println(r)

}
