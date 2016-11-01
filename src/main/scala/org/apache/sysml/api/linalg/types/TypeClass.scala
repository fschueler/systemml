package org.apache.sysml.api.linalg.types

import org.apache.spark.rdd.RDD
import org.apache.sysml.api.linalg.Lazy.{BinOp, Tree}
import org.apache.sysml.api.linalg.types.TypeClass.DenseBlock

object TypeClass {

  //////////////////////////////////////////////////////////////////////////////////////////////
  // BLOCK TYPECLASS
  //////////////////////////////////////////////////////////////////////////////////////////////

  /**
    * Represents the actual physical single node representation of the data
    */

  trait Block[A] {
    def +(x: A, y: A): A
  }

  implicit class BlockOps[A](a: A)(implicit ev: Block[A]) {
    def +(y: A) = ev.+(a, y)
  }

  case class DenseBlock(values: Array[Double])
  object DenseBlock {
    implicit val DenseBlock = new Block[DenseBlock] {
      override def +(x: DenseBlock, y: DenseBlock): DenseBlock = {
        DenseBlock(x.values.zip(y.values).map(x => x._1 + x._2))
      }
    }
  }

  case class SparseBlock(values: Map[(Int, Int), Double])
  object SparseBlock {
    implicit val SparseBlock = new Block[SparseBlock] {
      override def +(x: SparseBlock, y: SparseBlock) = {
        val result = for (key <- x.values.keys ++ y.values.keys) yield
          key -> (x.values.getOrElse(key, 0.0) + y.values.getOrElse(key, 0.0))
        SparseBlock(result.toMap[(Int, Int), Double])
      }
    }
  }

  case class ZeroBlock()
  object ZeroBlock {
      implicit val ZeroBlock = new Block[ZeroBlock] {
        override def +(x: ZeroBlock, y: ZeroBlock): ZeroBlock = new ZeroBlock()
      }
   }

  //////////////////////////////////////////////////////////////////////////////////////////////
  // LOCATION TYPECLASS
  //////////////////////////////////////////////////////////////////////////////////////////////

  /**
    * Represents the layout of the blocks (distributed, local, etc.)
    */

  trait Layout[A] {
    def +(x: A, y: A): A
  }

  implicit class LayoutOps[A](a: A)(implicit ev1: Layout[A]) {
    def +(y: A) = ev1.+(a, y)
  }

  case class Local[A: Block](impl: A)
  object Local {
    implicit def LocalLayout[A: Block] = new Layout[Local[A]] {
      override def +(x: Local[A], y: Local[A]): Local[A] = {
        Local(x.impl + y.impl)
      }
    }
  }

  case class Spark[A: Block](impl: RDD[((Int, Int), A)])
  object Spark {
    implicit def SparkLayout[A: Block] = new Layout[Spark[A]] {
      override def +(x: Spark[A], y: Spark[A]): Spark[A] = {
        Spark(x.impl.join(y.impl).map(x => (x._1, x._2._1 + x._2._2)))
      }

      def +(x: Spark[A], y: Local[A]) = {
        // distribute y, and call +(Spark, Spark)
      }
    }
  }

  case class LazyEval(impl: Tree)
  object LazyEval {
    implicit val lazyEval = new Layout[LazyEval] {
      override def +(x: LazyEval, y: LazyEval): LazyEval = {
        LazyEval(BinOp("+", x.impl, y.impl))
      }
    }
  }
}

