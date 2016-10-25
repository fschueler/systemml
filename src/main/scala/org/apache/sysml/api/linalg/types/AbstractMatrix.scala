package org.apache.sysml.api.linalg.types

abstract class AbstractMatrix {

  ////////////////////////////////////////
  // PUBLIC INTERFACE
  ////////////////////////////////////////

  def %*%(that: AbstractMatrix): AbstractMatrix

  def +(that: AbstractMatrix): AbstractMatrix
}

