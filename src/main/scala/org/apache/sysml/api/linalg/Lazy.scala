package org.apache.sysml.api.linalg

import org.apache.sysml.api.linalg.Distributions.{Normal, Poisson, Uniform}
import org.apache.sysml.api.linalg.Lazy._
import org.apache.sysml.api.linalg.types.TypeClass.{DenseBlock, EagerEval, LazyEval}
import org.apache.sysml.api.mlcontext.MLContext
import org.apache.sysml.api.mlcontext.ScriptFactory._

import scala.collection.mutable

trait Lazy {

  def eval(impl: Tree)(implicit mlctx: MLContext): EagerEval = {
    val dmlString = traverse(impl)
    println(
      s"""
         |executing script:
         |$dmlString
       """.stripMargin)
    val script = dml(dmlString).out("x0")
    val res = mlctx.execute(script)
    val mo = res.getMatrixObject("x0")
    EagerEval(mo)
  }

  def traverse(tree: Tree): String = {
    var counter = -1

    def freshName(): String = {
      counter += 1
      s"x$counter"
    }

    // an environment to avoid recomputation of already computed values
    val env = mutable.HashMap.empty[Tree, String] // holds the values and their declarations

    val statements = new StringBuilder()

    def constructStatements(tree: Tree): String = {
      // if we have already generated a statement that computes this expression, use a reference instead
      val part = if (env.contains(tree))
        env(tree)
        // else, generate new reference for the expression and add the statement
      else {
        val newName = freshName()
        env.put(tree, newName)

        val statement = tree match {
          // constructor for random matrix
          case Application(fname, args) if fname == "rand" => args match {
            case Scalar(rows) :: Scalar(cols) :: Literal(dist) :: Scalar(spars) :: Nil => dist match {
              case Uniform(a, b) => s"""$newName = rand(rows=${rows}, cols=${cols}, min=$a, max=$b, pdf="uniform", sparsity=$spars);\n"""

              case Normal() => s"""$newName = rand(rows=$rows, cols=$cols, pdf="normal", sparsity=$spars);\n"""

              case Poisson(lambda) => s"""$newName = rand(rows=$rows, cols=$cols, pdf="poisson", lambda=$lambda, sparsity=$spars);\n"""
            }
          }

          case Scalar(value) => s"$newName = ${value.toString};\n"

          case BinOp(rator, rand1, rand2) => s"""$newName = (${constructStatements(rand1)} $rator ${constructStatements(rand2)});\n"""
        }
        statements ++= statement
        newName
      }
      // return name of the old/new reference
      part
    }
    // collect statements from tree
    constructStatements(tree)
    statements.toString()
  }
}

object Lazy {

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
