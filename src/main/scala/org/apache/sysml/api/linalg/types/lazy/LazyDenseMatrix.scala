package org.apache.sysml.api.linalg.types.`lazy`

import org.apache.sysml.api.linalg.types.`lazy`.LazyMatrix.{BinaryOp, Node}
import org.apache.sysml.api.linalg.types.{AbstractMatrix, Dense, DenseBlock}

class LazyDenseMatrix(op: Node) extends LazyMatrix(op) with Dense {
  override val data: DenseBlock = _

  override def %*%(that: AbstractMatrix): AbstractMatrix = ???

  override def +(that: AbstractMatrix): AbstractMatrix = ???
}
