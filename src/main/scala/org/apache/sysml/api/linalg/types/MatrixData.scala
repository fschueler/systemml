package org.apache.sysml.api.linalg.types

/**
  * A trait that represents the backing data of a matrix. It is a universal trait to support Value classes.
  * A universal trait is a trait that extends Any, only has defs as members, and does no initialization.
  */
trait MatrixData {

}

class DenseBlock extends MatrixData
class LocalDenseBlock extends DenseBlock
class DistributedDenseBlock extends DenseBlock

class SparseBlock extends MatrixData