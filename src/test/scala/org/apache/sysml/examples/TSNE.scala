package org.apache.sysml.examples

import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api._

object TSNE extends App {

  val alg =  parallelize {
    def distanceMatrix(X: Matrix): Matrix = {
      val n = X.nrow
      val s = rowSums(X * X)

      println("Computing distance matrix...")

      (((X * -2.0) %*% X.t) + s) + s.t
    }

    def x2p(X: Matrix, perplexity: Double): Matrix = {
      val tol = 1.0e-3
      val INF = 1.0e20
      val n = X.nrow
      println("n: " + n)
      val D = distanceMatrix(X)

      var P = Matrix.zeros(n, n)
      var beta = Matrix.ones(n, 1)
      val logU = log(perplexity)

      println("Starting x2p for-loop...")

      for (i <- 0 until n) {
        println("i: " + i)

        var betamin = 0.0
        var betamax = INF
        var Hdiff = INF

        var itr = 0
        if (i % 500 == 0) {
          println(i)
        }
        val Di = D(i, :::)
        var Pi = Matrix.zeros(1, Di.nrow)

        // while ((abs(Hdiff) > tol) && (itr < 50)) {
        while (itr < 10) {
          Pi = exp(Di * -1.0 * beta(i, 0))

          Pi(0, i) = 0.0

          val sumPi = sum(Pi)

          val H = log(sumPi) + beta(i, 0) * sum(Di * Pi) / sumPi
          Pi = Pi / sumPi
          Hdiff = H - logU

          if (Hdiff > 0.0) {
            betamin = beta(i, 0) * 2.0
            if (betamax == INF) {
              beta(i, 0) = beta(i, 0) * 2.0
            } else {
              beta(i, 0) = beta(i, 0) / 2.0
            }
          } else {
            betamax = beta(i, 0)
            if (betamin == 0.0) {
              beta(i, 0) = beta(i, 0) / 2.0
            } else {
              beta(i, 0) = beta(i, 0) / 2.0
            }
          }
          itr = itr + 1
        }
        P(i, :::) = Pi // update a row/column with a vector or a range of rows/cols with a matrix
      }
      println("Done with for-loop!")
      P = P + P.t
      P / sum(P)
    }


    // ALGORITHM

    // parameters
    val X           = Matrix.rand(100, 100)
    val reducedDims = 2
    val perplexity  = 30
    val lr          = 300.0
    val momentum    = 0.9
    val maxIter     = 1000

    // algorithm
    val d = reducedDims
    val n = X.nrow
    val inter = x2p(X, perplexity)
    var P = inter * 4.0
    var Y = Matrix.rand(n, d)
    var dY = Matrix.zeros(n, d)
    var C = Matrix.zeros(maxIter / 10, 1)

    val ZERODIAG = Matrix.diag(-1.0, Y.nrow) + 1.0

    for (itr <- 0 until maxIter) {
      val D = distanceMatrix(Y)
      val Z = (1.0 / (D + 1.0)) * ZERODIAG
      val Q = Z / sum(Z)
      val W = (P - Q) * Z
      val sumW = rowSums(W)
      val g = Y * sumW - W %*% Y
      val dy = momentum * dY - lr * g

      Y = Y + dY
      Y = Y - colMeans(Y)

      if (itr % 10 == 0) {
        val p: Matrix = pmax(P, 1e-12)
        val q: Matrix = pmax(Q, 1e-12)
        val t: Matrix = p / q

        val r = itr / 10
        println("r: " + r)
        C(r, 0) = sum(P * log(t))
      }
      if (itr == 100) {
        P = P / 4.0
      }
    }
    (Y, C)
  }

  val res = alg.run()
  println(res)
}
