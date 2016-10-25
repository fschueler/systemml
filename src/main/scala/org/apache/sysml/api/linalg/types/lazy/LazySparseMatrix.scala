package org.apache.sysml.api.linalg.types.`lazy`

import org.apache.sysml.api.linalg.types.`lazy`.LazyMatrix.Node
import org.apache.sysml.api.linalg.types.{AbstractMatrix, Sparse, SparseBlock}

class LazySparseMatrix extends LazyMatrix with Sparse {
  override val opsTree: Node = _
  override val data: SparseBlock = _

  override def %*%(that: AbstractMatrix): AbstractMatrix = ???

  override def +(that: AbstractMatrix): AbstractMatrix = ???
}
