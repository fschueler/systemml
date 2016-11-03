package org.apache.sysml.api.linalg

import org.apache.sysml.api.linalg.types.TypeClass.{DenseBlock, LazyEval, Local}

object Lazy {
  private var counter = -1

  def freshName(): String = {
    counter += 1
    s"x$counter"
  }

  def eval(mat: Matrix[LazyEval]): Local[DenseBlock] = {
    mat.impl.eval
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
