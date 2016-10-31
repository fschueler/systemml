package org.apache.sysml.api.linalg

import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.sysml.api.mlcontext.MLContext
import org.apache.sysml.api.mlcontext.ScriptFactory._
import org.apache.sysml.api.linalg.Lazy.Tree

trait Lazy {
  val tree: Tree

  def toDataFrame: DataFrame

  def eval(sc: SparkContext): Matrix[LazyVector, LazyMatrix] = {
    val mlctx = new MLContext(sc)
    val script = dml(evalTree)
    val result = mlctx.execute(script)

    // get result from mlcontext
    Matrix.zeros(3, 3)
  }

  def evalTree: String = {
    // do a walk of the tree and generate DMLString
    "dml dummy"
  }
}

object Lazy {

  abstract class Tree()
  case class Empty() extends Tree
  case class Scalar[T: Numeric](value: T) extends Tree
  case class UnaryOp(rator: String, rand: Tree) extends Tree
  case class BinOp(rator: String, rand1: Tree, rand2: Tree) extends Tree
  case class Application(func: String, args: List[Any]) extends Tree

  implicit def toEager(mat: LazyMatrix): EagerMatrix = {
    // evaluate and pass data to a new eagermatrix
    new EagerMatrix()
  }
}
