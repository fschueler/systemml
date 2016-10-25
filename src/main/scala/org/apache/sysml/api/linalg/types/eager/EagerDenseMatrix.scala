package org.apache.sysml.api.linalg.types.eager

import org.apache.sysml.api.linalg.types.{AbstractMatrix, Dense, DenseBlock}

class EagerDenseMatrix extends EagerMatrix with Dense {
  override val data: DenseBlock = _

  override def %*%(that: AbstractMatrix): AbstractMatrix = ???

  override def +(that: AbstractMatrix): AbstractMatrix = ???
}
