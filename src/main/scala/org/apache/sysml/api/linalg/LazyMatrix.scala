package org.apache.sysml.api.linalg
import org.apache.spark.sql.DataFrame
import org.apache.sysml.api.linalg.Lazy.{Application, BinOp, Scalar, Tree, UnaryOp}
import org.apache.sysml.api.linalg.api.:::
import org.apache.sysml.api.linalg.types.data.DataContainer

import scala.collection.immutable.Range.Inclusive

case class LazyMatrix(val tree: Tree) extends Lazy {
  def rows = 0
  def cols = 0
  def impl: DataContainer[_] = null

  override def toDataFrame: DataFrame = ???
}

case class LazyVector(val tree: Tree) extends Lazy {

  override def toDataFrame: DataFrame = ???
}
