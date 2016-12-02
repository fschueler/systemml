package org.apache.sysml.examples

import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api._

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros

object TSNE {

  import EvalMacro._

  def distanceMatrix(X: Matrix): Matrix = {
    val n = X.rows
    val s = rowSums(X * X)

    (((X * -2.0)  %*% X.t) + s) + s.t
  }

  def x2p(X: Matrix, perplexity: Double): Matrix = {
    val tol = 1.0e-5
    val INF = 1.0e20
    val n   = X.rows
    val D   = distanceMatrix(X)

    var P     = Matrix.zeros(n, n)
    var beta  = Matrix.ones(n, 1)
    val logU  = log(perplexity)

    for (i <- 0 until n) {
      var betamin = 0.0
      var betamax = INF
      var Hdiff   = INF

      var itr = 0
      val Di  = D(i, :::)
      var Pi  = Matrix.zeros(Di.rows, 1)

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
          if (betamin ==  0.0) {
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

  def tsne(X            : Matrix,
           reducedDims  : Int,
           perplexity   : Int,
           lr           : Double,
           momentum     : Double,
           maxIter      : Int): (Matrix, Matrix) = {

    // parameters
    val d   = reducedDims
    val n   = X.rows
    var P   = x2p(X, perplexity) * 4.0
    var Y   = Matrix.rand(n, d)
    var dY  = Matrix.zeros(n, d)
    var C   = Matrix.zeros(maxIter / 10, 1)

    val ZERODIAG = Matrix.diag(Vector.fill(n)(i => -1)) + 1.0

    for (itr <- 0 until maxIter) {
      val D     = distanceMatrix(Y)
      val Z     = (1.0 / (D + 1.0)) * ZERODIAG
      val Q     = Z / sum(Z)
      val W     = (P - Q) * Z
      val sumW  = rowSums(W)
      val g     = Y * sumW - W %*% Y
      val dy    = momentum * dY - lr * g

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
    eval(Y, C)
  }
}

object EvalMacro {
  //def eval[T <: Product](e: T): T = macro EvalMacroImpl.impl[T]
  def eval[T <: Product](e: T): T = ???
}

object EvalMacroImpl {
  def impl[T: c.WeakTypeTag](c: Context)(e: c.Expr[T]): c.Expr[T] = {
    e
  }
}
