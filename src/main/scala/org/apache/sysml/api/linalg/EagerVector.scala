package org.apache.sysml.api.linalg
import org.apache.sysml.api.linalg.api.:::
import org.apache.sysml.api.linalg.types.data.DataContainer

import scala.collection.immutable.Range.Inclusive

class EagerVector extends Matrix[EagerVector, EagerMatrix] {
  override def rows = 0

  override def cols = 0

  override def impl: DataContainer[_] = ???

  override def apply(row: Int, col: Int): Double = ???

  override def apply(row: Int, col: :::.type): EagerVector = ???

  override def apply(row: :::.type, col: Int): EagerVector = ???

  override def apply(rows: Inclusive, cols: :::.type): EagerMatrix = ???

  override def apply(rows: :::.type, cols: Inclusive): EagerMatrix = ???

  override def apply(rows: Inclusive, cols: Inclusive): EagerMatrix = ???

  // MODO make sure that the orientation of the vector (row/col) fits the assignment
  override def update(row: Int, col: Int, value: Double): EagerMatrix = ???

  override def update(row: Int, col: :::.type, vec: EagerVector): EagerMatrix = ???

  override def update(row: :::.type, col: Int, vec: EagerVector): EagerMatrix = ???

  override def update(rows: Inclusive, cols: :::.type, mat: EagerMatrix): EagerMatrix = ???

  override def update(rows: :::.type, cols: Inclusive, mat: EagerMatrix): EagerMatrix = ???

  override def update(rows: Inclusive, cols: Inclusive, mat: EagerMatrix): EagerMatrix = ???

  override def +(that: Double): EagerMatrix = ???

  override def -(that: Double): EagerMatrix = ???

  override def *(that: Double): EagerMatrix = ???

  override def /(that: Double): EagerMatrix = ???

  override def +(that: EagerVector): EagerMatrix = ???

  override def -(that: EagerVector): EagerMatrix = ???

  override def *(that: EagerVector): EagerMatrix = ???

  override def /(that: EagerVector): EagerMatrix = ???

  override def +(that: EagerMatrix): EagerMatrix = ???

  override def -(that: EagerMatrix): EagerMatrix = ???

  override def *(that: EagerMatrix): EagerMatrix = ???

  override def /(that: EagerMatrix): EagerMatrix = ???

  override def %*%(that: EagerMatrix): EagerMatrix = ???

  override def %*%(that: EagerVector): EagerVector = ???

  override def t: Matrix[EagerVector, EagerMatrix] = ???

  override def ^(n: Int): Matrix[EagerVector, EagerMatrix] = ???

  override def map(f: (Double) => Double): Matrix[EagerVector, EagerMatrix] = ???

  /**
    * Reshapes the [[Matrix]] into a new format. cols * rows must equal the original number of elements.
    *
    * @param rows  number of rows of the new M
    * @param cols  number of columns of the new M
    * @param byRow if true, Mis reshaped my row
    * @return new Mwith the new dimensions and rearranged values
    */
  override def reshape(rows: Int, cols: Int, byRow: Boolean): Matrix[EagerVector, EagerMatrix] = ???

  override def copy: Matrix[EagerVector, EagerMatrix] = ???
}
