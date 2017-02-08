package org.apache.sysml.api.linalg

trait SystemMLAlgorithm[T] {
  val inputs: Seq[(String, Any)]
  val outputs: Seq[String]

  def run(): T
}

