package org.apache.sysml.api.linalg

import org.apache.sysml.api.linalg.Distributions.{Normal, Poisson, Uniform}
import org.apache.sysml.api.linalg.Lazy._
import org.apache.sysml.api.linalg.types.TypeClass.{DenseBlock, EagerEval, LazyEval}
import org.apache.sysml.api.mlcontext.MLContext
import org.apache.sysml.api.mlcontext.ScriptFactory._

trait Lazy {
  private val id: String = Lazy.freshName()

  val statements = new StringBuilder

  def eval(impl: Tree)(implicit mlctx: MLContext): Matrix[EagerEval] = {
    val dmlString = traverse(impl)
    println(
      s"""
         |executing script:
         |$dmlString
       """.stripMargin)
    val script = dml(dmlString)
    val res = mlctx.execute(script)
    new Matrix(EagerEval(res.getMatrixObject(id)), 2, 2)
  }

  def traverse(tree: Tree): String = {
    tree match {
      // constructor for random matrix
      case Application(fname, args) if fname == "rand" => args match {
        case Scalar(rows) :: Scalar(cols) :: Literal(dist) :: Scalar(spars) :: Nil => dist match {
          case Uniform(a, b) =>
            s"""rand(rows=${rows}, cols=${cols}, min=$a, max=$b, pdf="uniform", sparsity=$spars)"""
          case Normal() =>
            s"""rand(rows=$rows, cols=$cols, pdf="normal", sparsity=$spars)"""
          case Poisson(lambda) =>
            s"""rand(rows=$rows, cols=$cols, pdf="poisson", lambda=$lambda, sparsity=$spars)"""
        }
      }

      case Scalar(value) => value.toString

      case BinOp(rator, rand1, rand2) => s"""(${traverse(rand1)} $rator ${traverse(rand2)})"""

      case VarDef(lhs, rhs) => s"""$lhs = ${traverse(rhs)}"""
    }
  }
}

object Lazy {
  private var counter = -1

  def freshName(): String = {
    counter += 1
    s"x$counter"
  }

  sealed abstract class Tree()
  case class Empty() extends Tree
  case class Scalar[T: Numeric](value: T) extends Tree
  case class TSeq[T: Numeric](seq: Seq[T]) extends Tree
  case class UnaryOp(rator: String, rand: Tree) extends Tree
  case class BinOp(rator: String, rand1: Tree, rand2: Tree) extends Tree
  case class Application(func: String, args: List[Tree]) extends Tree
  case class Literal(value: Any) extends Tree
  case class VarDef(name: String, rhs: Tree) extends Tree
}
