package org.apache.sysml.api.linalg.types.data

/**
  * A container for the data contained in a matrix. This let's us handle distributed and local data differently
  * while the underlying representation will be the same.
  */
abstract class DataContainer[+T] {
  def values: T
}

class LocalDataContainer[+T](override val values: T) extends DataContainer

class DistributedDataContainer[+T](override val values: Seq[T]) extends DataContainer




