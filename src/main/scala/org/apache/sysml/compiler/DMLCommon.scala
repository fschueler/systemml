package org.apache.sysml.compiler

import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.sysml.api.linalg.Matrix
import org.emmalanguage.compiler.Common

trait DMLCommon extends Common {

  // --------------------------------------------------------------------------
  // API
  // --------------------------------------------------------------------------

  override val rootPkg = "org.apache.sysml.api.linalg"

  protected[sysml] object DMLAPI extends IRModule {

    val apiModuleSymbol = api.Sym[org.apache.sysml.api.linalg.api.`package`.type].asModule
    val matrixSymbol = api.Sym[org.apache.sysml.api.linalg.Matrix].asClass
    val matrixModuleSymbol = matrixSymbol.companion.asModule
    def module = apiModuleSymbol

    private def matrixOp(name: String) = methodIn(matrixSymbol, name)

    // Sources
    val zeros               = methodIn(matrixModuleSymbol, "zeros")
    val ones                = methodIn(matrixModuleSymbol, "ones")
    val rand                = methodIn(matrixModuleSymbol, "rand")
    val fromDataFrame       = methodIn(matrixModuleSymbol, "fromDataFrame")


    // builtin functions
    val sum       = methodIn(apiModuleSymbol, "sum")
    val mean      = methodIn(apiModuleSymbol, "mean")
    val max      = methodIn(apiModuleSymbol, "max")

    // matrix operators
    val transpose = matrixOp("t")
    val matmult   = matrixOp("%*%")

    val sourceOps   = Set(zeros, ones, rand, fromDataFrame)
    val builtinOps  = Set(sum, mean, max)
    val matOps      = Set(transpose, matmult)

    val ops = sourceOps | builtinOps | matOps

    val modules = Set(apiModuleSymbol, matrixModuleSymbol)

    // type constructors
    val Matrix      = api.Type[Matrix].typeConstructor
    val DataFrame   = api.Type[DataFrame].typeConstructor
    val Double      = api.Type[Double].typeConstructor
    val Int         = api.Type[Int].typeConstructor

    // Set of valid inputs to the macro
    val inputs = Set(Matrix, DataFrame)
    val primitives = Set(Double, Int)


  }
}
