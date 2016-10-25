package org.apache.sysml.api.linalg.types.`lazy`

import org.apache.sysml.api.linalg.types.AbstractMatrix
import org.apache.sysml.api.linalg.types.`lazy`.LazyMatrix.Node

abstract class LazyMatrix(val opsTree: Node) extends AbstractMatrix {

}

object LazyMatrix {
  sealed case class Node()
  case class BinaryOp(rator: String, rand1: Node, rand2: Node) extends Node
  case class UnaryOp(rator: String, rand: Node)

  def eval: Node => AbstractMatrix = ???
}