package org.apache.sysml.api.linalg.types.eager

import org.apache.sysml.api.linalg.types.{AbstractMatrix, Sparse, SparseBlock}

class EagerSparseMatrix extends EagerMatrix with Sparse {
  override val data: SparseBlock = _

  override def %*%(that: AbstractMatrix): AbstractMatrix = ???

  override def +(that: AbstractMatrix): AbstractMatrix = ???

}
