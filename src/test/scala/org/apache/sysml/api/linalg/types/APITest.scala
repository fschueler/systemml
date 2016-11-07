package org.apache.sysml.api.linalg.types

import org.apache.sysml.api.linalg.{Lazy, Matrix}
import org.apache.sysml.api.linalg.api._
import org.apache.sysml.api.linalg.Distributions._

object APITest extends App {

    val A = Matrix.rand(2, 2, Normal(), 0.2)

    val B = Matrix.rand(2, 2, Uniform(0.0, 1.0), 0.7)

    var r = A + B

    println(r.collect())
}
