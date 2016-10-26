package org.apache.sysml.api.linalg.types

import org.apache.sysml.api.linalg.types.eager.EagerMatrix

object APITest {

  def main(args: Array[String]) = {
    val A = AbstractMatrix.zeros(3, 3)
    val B = AbstractMatrix.zeros(3, 4)
    val C = A + B

    C
  }

}
