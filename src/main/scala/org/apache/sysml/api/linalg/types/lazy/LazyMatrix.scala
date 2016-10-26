package org.apache.sysml.api.linalg.types.`lazy`

import org.apache.sysml.api.linalg.types.AbstractMatrix
import org.apache.sysml.api.linalg.types.data.{DataContainer, MatrixValue}
import org.apache.sysml.api.linalg.types.eager.EagerMatrix

/**
  * A matrix with lazy evaluation semantics.
  *
  * @param tree
  */
class LazyMatrix(override private val data: DataContainer[MatrixValue], val tree: Tree) extends AbstractMatrix {
  // the underlying data. This will be empty be default until eval is called.

  override def +(that: AbstractMatrix): AbstractMatrix = that match {
    case a: EagerMatrix => // define what happens here

      /* in this case we don't have to care about the actual data. to convert back to an eager matrix we should still
         keep the info about what the actual Container and Value types are.
       */
    case a: LazyMatrix  => new LazyMatrix(a.data, BinaryOp("+", this.tree, a.tree))
  }

  // populates the data field
  def eval(): LazyMatrix = {

  }

  // constructor of EagerMatrix calls eval and uses underlying data as its data to keep on going
  def toEagerMatrix: EagerMatrix = {
    this.eval()
    new EagerMatrix(this.data)
  }

}

sealed abstract class Tree()
case class Empty() extends Tree
case class BinaryOp(rator: String, rand1: Tree, rand2: Tree) extends Tree