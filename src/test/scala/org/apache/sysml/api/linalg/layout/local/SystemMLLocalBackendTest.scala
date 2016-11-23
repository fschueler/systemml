package org.apache.sysml.api.linalg.layout.local

import breeze.stats.distributions.Rand
import org.apache.sysml.api.BaseAPISpec
import org.apache.sysml.api.linalg.Distributions.Normal
import org.apache.sysml.api.linalg.layout.DenseMatrix
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.fommil.netlib.BLAS
import org.apache.sysml.runtime.matrix.data.{LibMatrixMult, MatrixBlock}

@RunWith(classOf[JUnitRunner])
class SystemMLLocalBackendTest extends BaseAPISpec {

  "Matrix" - {
    "Multiplication" - {
      val m, n = 1000

      "Compare Breeze and SystemML" in {
        println("Using " + BLAS.getInstance().getClass().getName())

        // initialize breeze matrices
        val A_breeze = breeze.linalg.DenseMatrix.rand[Double](m, n, Rand.gaussian)
        val B_breeze = breeze.linalg.DenseMatrix.rand[Double](n, m, Rand.gaussian)
        var C_breeze = breeze.linalg.DenseMatrix.rand[Double](n, m, Rand.gaussian)


        // copy the data from breeze matrices to new arrays for systemml
        val data_a = Array.fill(m * n)(0.0)
        val data_b = Array.fill(m * n)(0.0)
        val data_c = Array.fill(m * m)(0.0)

        A_breeze.toArray.copyToArray(data_a)
        B_breeze.toArray.copyToArray(data_b)
        C_breeze.toArray.copyToArray(data_c)

        // initialize systemml matrix blocks
        val A_sysml = new MatrixBlock(m, n, false, 0L); A_sysml.init(data_a, m, n)
        val B_sysml = new MatrixBlock(n, m, false, 0L); B_sysml.init(data_b, n, m)
        val C_sysml = new MatrixBlock(m, m, false, 0L); C_sysml.init(data_c, m, m)

        val runs = 1

        val startBreeze = System.currentTimeMillis()
        for (i <- 1 to runs) {
          C_breeze = A_breeze * B_breeze
        }
        val endBreeze = System.currentTimeMillis()


        val startSystemML = System.currentTimeMillis()
        for (i <- 1 to runs) {
          LibMatrixMult.matrixMult(A_sysml, B_sysml, C_sysml, 8)
        }
        val endSystemML = System.currentTimeMillis()
        
        println(
          s"""
             |Breeze:   ${(endBreeze - startBreeze) / runs.toDouble} ms
             |SystemML: ${(endSystemML - startSystemML) / runs.toDouble} ms
           """.stripMargin)

      }
    }
  }
}
