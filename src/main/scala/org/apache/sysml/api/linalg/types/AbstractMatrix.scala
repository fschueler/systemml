package org.apache.sysml.api.linalg.types

import org.apache.sysml.api.linalg.types.data._
import org.apache.sysml.api.linalg.types.eager.EagerMatrix

import scala.util.Random

abstract class AbstractMatrix {

  protected def data: DataContainer[MatrixValue]

  ////////////////////////////////////////
  // PUBLIC INTERFACE
  ////////////////////////////////////////

  def +(that: AbstractMatrix): AbstractMatrix

}

object AbstractMatrix {
  // maximum size for a local matrix
  val MAXLOCALSIZE: Int = 10000

  def zeros(rows: Int, cols: Int): AbstractMatrix = {
    if (rows * cols < MAXLOCALSIZE) {
      new EagerMatrix(new LocalDataContainer(new ZeroBlock(rows, cols)))
    } else {
      new EagerMatrix(new DistributedDataContainer(Seq(new ZeroBlock(rows, cols)))) // we don't actually have to parallelize here
    }
  }

  def rand(rows: Int, cols: Int): AbstractMatrix = {
    val rand = new Random(1L)

    if (rows * cols < MAXLOCALSIZE) {
      new EagerMatrix(new LocalDataContainer(new DenseBlock(Array.fill[Array[Double]](rows)(Array.fill[Double](cols)(rand.nextDouble())))))
    } else {
      new EagerMatrix(new DistributedDataContainer(/* code that distributes data and returns handles as Seq[DenseBlock]] */))
    }
  }

  def read(path: String): AbstractMatrix = {
      /* read the matrix, depending from where return either
            - EagerMatrix[LocalDataContainer[A]]
            - EagerMatrix[DistributedContainer[A]]

            where type A should be inferred while reading the matrix.
       */
    new EagerMatrix(new LocalDataContainer(new ZeroBlock(1, 1)))
  }
}

