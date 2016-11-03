package org.apache.sysml.api.linalg

import org.apache.sysml.api.linalg.types.TypeClass.Strategy


abstract class Vector[A: Strategy](override val impl: A, rows: Int, cols: Int) extends Matrix[A](impl, rows, 1) {

}

object Vector {

}