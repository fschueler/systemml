package org.apache.sysml.api.linalg.types.eager

import org.apache.sysml.api.linalg.types.AbstractMatrix
import org.apache.sysml.api.linalg.types.`lazy`.{Empty, LazyMatrix, Tree}
import org.apache.sysml.api.linalg.types.data._

class EagerMatrix(override private val data: DataContainer[MatrixValue]) extends AbstractMatrix {

  def this(lazyMatrix: LazyMatrix) = {
    this(lazyMatrix)
  }

  // do operations based on container (dist, local) and value (sparse/dense)
  override def +(that: AbstractMatrix): AbstractMatrix = (this.data, that.data) match {

    case (a: LocalDataContainer[_], b: LocalDataContainer[_]) => (a.values, b.values) match {
      case (v: ZeroBlock, w: _) =>              // always return w since zeroblock is the neutral element
      case (v: _, w: ZeroBlock) =>              // same
      case (v: DenseBlock, w: DenseBlock) =>    new EagerMatrix(new LocalDataContainer[DenseBlock](v + w))
      case (v: SparseBlock, w: DenseBlock) =>   // add sparse and dense blocks
      case (v: SparseBlock, w: SparseBlock) =>  // add two sparse blocks
    }// perform local operation
    case (a: LocalDataContainer[_], b: DistributedDataContainer[_]) => // either parallelize a or collect b
    case (a: DistributedDataContainer[_], b: DistributedDataContainer[_]) => // perform distributed operation
  }

  // starts a new empty tree
  def toLazyMatrix: LazyMatrix = {
    new LazyMatrix(this.data, Empty())
  }

}
