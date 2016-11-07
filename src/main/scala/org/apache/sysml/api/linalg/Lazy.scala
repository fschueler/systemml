package org.apache.sysml.api.linalg

import org.apache.sysml.api.linalg.Distributions.{Normal, Poisson, Uniform}
import org.apache.sysml.api.linalg.Lazy._
import org.apache.sysml.api.linalg.types.TypeClass.{DenseBlock, EagerEval, LazyEval}
import org.apache.sysml.api.mlcontext.MLContext
import org.apache.sysml.api.mlcontext.ScriptFactory._

trait Lazy {

  def eval(impl: Tree)(implicit mlctx: MLContext): Matrix[EagerEval] = {
    val dmlString = traverse(impl)
    println(
      s"""
         |executing script:
         |$dmlString
       """.stripMargin)
    val script = dml(dmlString).out("x0")
    val res = mlctx.execute(script)
    val mo = res.getMatrixObject("x0")
    new Matrix(EagerEval(mo), mo.getNumRows.toInt, mo.getNumColumns.toInt)
  }

  def traverse(tree: Tree): String = {

    var statements = new StringBuilder()

    def constructStatements(tree: Tree): String = {tree match {
        // constructor for random matrix
        case Application(fname, args) if fname == "rand" => args match {
          case Scalar(rows) :: Scalar(cols) :: Literal(dist) :: Scalar(spars) :: Nil => dist match {
            case Uniform(a, b) => {
              val name = freshName()
              statements ++= s"""$name = rand(rows=${rows}, cols=${cols}, min=$a, max=$b, pdf="uniform", sparsity=$spars);\n"""
              name
            }
            case Normal() => {
              val name = freshName()
              statements ++= s"""$name = rand(rows=$rows, cols=$cols, pdf="normal", sparsity=$spars);\n"""
              name
            }
            case Poisson(lambda) => {
              val name = freshName()
              statements ++= s"""$name = rand(rows=$rows, cols=$cols, pdf="poisson", lambda=$lambda, sparsity=$spars);\n"""
              name
            }
          }
        }

        case Scalar(value) => {
          val name = freshName()
          statements ++= s"$name = ${value.toString};\n"
          name
        }

        case BinOp(rator, rand1, rand2) => {
          val name = freshName()
          statements ++= s"""$name = (${constructStatements(rand1)} $rator ${constructStatements(rand2)});\n"""
          name
        }
     }
    }
    constructStatements(tree)
    statements.toString()
  }
}

object Lazy {

  // TODO build an environment to avoid recomputation of already computed values
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
