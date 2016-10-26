package org.apache.sysml.api.linalg
import org.apache.sysml.api.linalg.api.:::

import scala.collection.immutable.Range.Inclusive

private class EagerMatrix extends Matrix[EagerMatrix] with Eager {
  override val rows: Int = _
  override val cols: Int = _

  override def apply(row: Int, col: Int): Double = ???

  override def apply(row: Int, col: :::.type): Vector = ???

  override def apply(row: :::.type, col: Int): Vector = ???

  override def apply(rows: Inclusive, cols: :::.type): EagerMatrix = ???

  override def apply(rows: :::.type, cols: Inclusive): EagerMatrix = ???

  override def apply(rows: Inclusive, cols: Inclusive): EagerMatrix = ???

  // TODO make sure that the orientation of the vector (row/col) fits the assignment
  override def update(row: Int, col: Int, value: Double): EagerMatrix = ???

  override def update(row: Int, col: :::.type, vec: Vector): EagerMatrix = ???

  override def update(row: :::.type, col: Int, vec: Vector): EagerMatrix = ???

  override def update(rows: Inclusive, cols: :::.type, mat: EagerMatrix): EagerMatrix = ???

  override def update(rows: :::.type, cols: Inclusive, mat: EagerMatrix): EagerMatrix = ???

  override def update(rows: Inclusive, cols: Inclusive, mat: EagerMatrix): EagerMatrix = ???

  override def +(that: Double): EagerMatrix = ???

  override def -(that: Double): EagerMatrix = ???

  override def *(that: Double): EagerMatrix = ???

  override def /(that: Double): EagerMatrix = ???

  override def +(that: Vector): EagerMatrix = ???

  override def -(that: Vector): EagerMatrix = ???

  override def *(that: Vector): EagerMatrix = ???

  override def /(that: Vector): EagerMatrix = ???

  override def +(that: EagerMatrix): EagerMatrix = ???

  override def -(that: EagerMatrix): EagerMatrix = ???

  override def *(that: EagerMatrix): EagerMatrix = ???

  override def /(that: EagerMatrix): EagerMatrix = ???

  override def %*%(that: EagerMatrix): EagerMatrix = ???

  override def %*%(that: Vector): Vector = ???

  override def t: EagerMatrix = ???

  override def ^(n: Int): EagerMatrix = ???

  override def map(f: (Double) => Double): EagerMatrix = ???

  /**
    * Reshapes the [[EagerMatrix]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows  number of rows of the new T
    * @param cols  number of columns of the new T
    * @param byRow if true, T is reshaped my row
    * @return new T with the new dimensions and rearranged values
    */
  override def reshape(rows: Int, cols: Int, byRow: Boolean): EagerMatrix = ???

  override def copy: EagerMatrix = ???
}
