package org.apache.sysml.examples

import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api._

object TSNE extends App {

  val alg =  parallelize {
    def distanceMatrix(X: Matrix): Matrix = {
      val n = X.nrow
      val s = rowSums(X * X)

      (((X * -2.0) %*% X.t) + s) + s.t
    }

    def x2p(X: Matrix, perplexity: Double): Matrix = {
      val tol = 1.0e-5
      val INF = 1.0e20
      val n = X.nrow
      val D = distanceMatrix(X)

      var P = Matrix.zeros(n, n)
      var beta = Matrix.ones(n, 1)
      val logU = log(perplexity)

      for (i <- 0 until n) {
        var betamin = 0.0
        var betamax = INF
        var Hdiff = INF

        var itr = 0
        val Di = D(i, :::)
        var Pi = Matrix.zeros(Di.nrow, 1)

        while ((abs(Hdiff) > tol) && (itr < 50)) {
          Pi = exp(Di * -1 * beta(i, 1))

          Pi(i, 1) = 0.0

          val sumPi = sum(Pi)

          val H = log(sumPi) + beta(i, 1) * sum(Di * Pi) / sumPi
          Pi = Pi / sumPi
          Hdiff = H - logU

          if (Hdiff > 0.0) {
            betamin = beta(i, 1) * 2.0
            if (betamax == INF) {
              beta(i, 1) = beta(i, 1) * 2
            } else {
              beta(i, 1) = beta(i, 1) / 2.0
            }
          } else {
            betamax = beta(i, 1)
            if (betamin == 0.0) {
              beta(i, 1) = beta(i, 1) / 2.0
            } else {
              beta(i, 1) = beta(i, 1) / 2.0
            }
          }
          itr = itr + 1
        }
        P(i, :::) = Pi // update a row/column with a vector or a range of rows/cols with a matrix
      }
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
    var P = x2p(X, perplexity) * 4.0
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

        C(itr / 10, 1) = sum(P * log(t))
      }
      if (itr == 100) {
        P = P / 4
      }
    }
    (Y, C)
  }

  val res = alg.run()
  println(res)
}
