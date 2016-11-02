package org.apache.sysml.api.linalg.types

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.{PairRDDFunctions, RDD}
import org.apache.spark.SparkContext._
import org.apache.sysml.api.linalg.Lazy.{BinOp, Tree}

import scala.reflect.ClassTag

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
    implicit val denseBlock = new Block[DenseBlock] {
      override def +(x: DenseBlock, y: DenseBlock): DenseBlock = {
        DenseBlock(x.values.zip(y.values).map(x => x._1 + x._2))
      }
    }
  }

  case class SparseBlock(values: Map[(Int, Int), Double])
  object SparseBlock {
    implicit val sparseBlock = new Block[SparseBlock] {
      override def +(x: SparseBlock, y: SparseBlock) = {
        val result = for (key <- x.values.keys ++ y.values.keys) yield
          key -> (x.values.getOrElse(key, 0.0) + y.values.getOrElse(key, 0.0))
        SparseBlock(result.toMap[(Int, Int), Double])
      }
    }
  }

  case class ZeroBlock()
  object ZeroBlock {
      implicit val zeroBlock = new Block[ZeroBlock] {
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
    // add two layouts of the same type
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
    private val sc = new SparkContext(new SparkConf().setMaster("local[2]").setAppName("TypeclassTest"))

    implicit def SparkLayout[A: Block : ClassTag]: Layout[Spark[A]] = new Layout[Spark[A]] {

      override def +(x: Spark[A], y: Spark[A]): Spark[A] = {
        def joined = x.impl.join(y.impl)
        val mapped = joined.map { case (k, (v, w)) => (k, v + v)}
        Spark(mapped)
      }

      def +(x: Spark[A], y: Local[A]) = {
        // distribute y, and call +(Spark, Spark)
      }
    }

    def apply(block: DenseBlock): Spark[DenseBlock] = Spark(sc.parallelize(Seq(((1, 1), block))))
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

