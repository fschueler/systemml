package org.apache.sysml.api.linalg.types

import org.apache.spark.rdd.RDD
import org.apache.sysml.api.linalg.Lazy.Tree

object TypeClass {

  //////////////////////////////////////////////////////////////////////////////////////////////
  // BLOCK TYPECLASS
  //////////////////////////////////////////////////////////////////////////////////////////////

  /**
    * Represents the actual physical single node representation of the data
    */
  abstract case class Block()
  case class DenseBlock(values: Array[Double]) extends Block
  case class SparseBlock(values: Map[(Int, Int), Double]) extends Block
  case class ZeroBlock() extends Block

  trait BlockOps[A] {
    def +(x: A, y: A): A
  }

  object BlockOps {

    implicit object SparseBlockOps extends BlockOps[SparseBlock] {
      override def +(x: SparseBlock, y: SparseBlock) = {
        val result = for (key <- x.values.keys ++ y.values.keys) yield
                        key -> (x.values.getOrElse(key, 0.0) + y.values.getOrElse(key, 0.0))

        SparseBlock(result.toMap[(Int, Int), Double])
      }
    }

    implicit object DenseBlockOps extends BlockOps[DenseBlock] {
      override def +(x: DenseBlock, y: DenseBlock): DenseBlock = {
        DenseBlock(x.values.zip(y.values).map(x => x._1 + x._2))
      }
    }

    implicit object ZeroBlockOps extends BlockOps[ZeroBlock] {
      override def +(x: ZeroBlock, y: ZeroBlock): ZeroBlock = {
        ZeroBlock()
      }

      def +(x: DenseBlock, y: ZeroBlock): DenseBlock = x
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////
  // LOCATION TYPECLASS
  //////////////////////////////////////////////////////////////////////////////////////////////

  /**
    * Represents the layout of the blocks (distributed, local, etc.)
    */
  abstract case class MatrixValue()
  case class Spark(impl: RDD[((Int, Int), Block)]) extends MatrixValue
  case class Local() extends MatrixValue


  trait Layout[A] {
    def +(x: A, y: A): A
  }

  object Layout {

  }

  //////////////////////////////////////////////////////////////////////////////////////////////
  // EVALUATION Strategy
  //////////////////////////////////////////////////////////////////////////////////////////////
  // Matrix type class
  case class Lazy(impl: Tree)
  case class Eager(impl: MatrixValue)

  trait MatrixOps[A] {
    def +(x: A, y: A): A
    def %*%(x: A, y: A): A
  }

  object MatrixOps {

  }
}

