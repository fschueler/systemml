package org.apache.sysml.api.linalg.layout

import org.apache.sysml.api.linalg.Distributions.Distribution

trait MatrixConstructors {

  def apply(vals: Array[Array[Double]]): Matrix
  def rand(rows: Int, cols: Int, dist: Distribution): Matrix
  def zeros(rows: Int, cols: Int): Matrix
  def eye(elemens: Int): Matrix
}
