package org.apache.sysml.examples

import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api.{SystemMLAlgorithm, _}

object NMF {

  def main(args: Array[String]) = {

    val nmf: SystemMLAlgorithm[(Matrix, Matrix)] = parallelize {
      val tfidf = Array(1.0, 2.0, 3.0, 4.0) // tfidf feature matrix coming from somewhere
      val k = 40
      val m, n = 2 // dimensions of tfidf
      val maxIters = 200

      val V = Matrix(tfidf, m, n) // initialize matrices
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
}
