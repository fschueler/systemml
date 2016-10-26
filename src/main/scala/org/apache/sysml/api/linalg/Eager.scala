package org.apache.sysml.api.linalg

import org.apache.sysml.api.linalg.Lazy.Empty

trait Eager {

}

object Eager {
  implicit def toLazy(mat: EagerMatrix): LazyMatrix = {
    // evaluate and pass data to a new eagermatrix
    new LazyMatrix(Empty())
  }
}
