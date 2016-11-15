package org.apache.sysml.api.linalg.data

import org.apache.sysml.api.linalg.Distributions.{Distribution, Normal}
import org.apache.sysml.api.linalg.layout.{DenseMatrix, SparseMatrix}
import org.apache.sysml.runtime.matrix.data.{LibMatrixDatagen, MatrixBlock}

object ScratchPad extends App {


    val tfidf = SparseMatrix.rand(1000, 100, 0.1)

    val k = 40
    val m, n = 2 // dimensions of tfidf
    val maxIters = 200

    val V = tfidf
    var W = DenseMatrix.rand(m, k, Normal())
    var H = SparseMatrix.rand(k, n, Normal())

    for (i <- 0 to maxIters) {
      //main loop

      val t1 = W.t %*% V
      val t2 = W %*% H
      val t3 = W.t %*% t2

      H = H * (t1 / t3)
      W = W * (V %*% H.t) / (W %*% (H %*% H.t))
    }

    (W, H) // return values
}





