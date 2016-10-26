package org.apache.sysml.api.linalg.types.data

/**
  * This is the actual underlying representation that holds pointers to the data.
  * We can have a variety of formats with different semantics attached and different physical representation of
  * their respective values.
  *
  * It might be worth thinking about putting operations directly into those formats.
  */
abstract class MatrixValue

abstract class MatrixBlock extends MatrixValue {
  type T
  def values: T
  def rlen: Int
  def clen: Int
  def nnz: Int
}

class DenseBlock(override val values: Array[Array[Double]]) extends MatrixBlock {
  override type T = Array[Array[Double]]

  override def rlen: Int = ???

  override def clen: Int = ???

  lazy val nnz = values.flatten.count(_ != 0.0)

  def +(that: DenseBlock): DenseBlock = {
    val zipped = this.values.zip(that.values)
    new DenseBlock(zipped.map(a => a._1.zip(a._2).map(b => b._1 + b._2)))
  }

  def +(that: SparseBlock): DenseBlock = ???
}

abstract class SparseBlock extends MatrixBlock

class ConstantBlock(val values: Double,
                    override val rlen: Int,
                    override val clen: Int) extends MatrixBlock {

  override lazy val nnz = if (values == 0.0) 0 else rlen * clen
}

class ZeroBlock(override val rlen: Int,
                override val clen: Int) extends ConstantBlock(0.0, rlen, clen)



