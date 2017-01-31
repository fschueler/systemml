package org.apache.sysml.compiler

import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.sysml.api.linalg.Matrix
import org.emmalanguage.compiler.Common

trait DMLCommon extends Common {

  class Environment(val inputs: Map[String, u.TermSymbol],
                    val bindingRefs: Map[String, u.TermSymbol],
                    val offset: Int) {

  }

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
    private def matrixOp(name: String, paramType: u.Type) = methodIn(matrixSymbol, name, paramType)

    def methodIn(target: u.Symbol, name: String, paramTpe: u.Type): u.MethodSymbol = {
      val alternatives = target.info.member(api.TermName(name)).alternatives

      val res = alternatives.find { alternative =>
        alternative.typeSignature.paramLists.flatten.headOption match {
          case Some(tpe) => tpe.typeSignature.finalResultType =:= paramTpe
          case None => false
        }
      }

      res match {
        case Some(sym) => sym.asMethod
        case None => abort(s"Could not create method symbol for target: $target, name: $name, parameter type: $paramTpe")
      }
    }

    // type constructors
    val Matrix      = api.Type[Matrix].typeConstructor
    val DataFrame   = api.Type[DataFrame].typeConstructor
    val Double      = api.Type[Double].typeConstructor
    val Int         = api.Type[Int].typeConstructor

    // Sources
    val zeros               = methodIn(matrixModuleSymbol, "zeros")
    val ones                = methodIn(matrixModuleSymbol, "ones")
    val rand                = methodIn(matrixModuleSymbol, "rand")
    val fromDataFrame       = methodIn(matrixModuleSymbol, "fromDataFrame")


    // builtin functions
    val sum       = methodIn(apiModuleSymbol, "sum")
    val mean      = methodIn(apiModuleSymbol, "mean")
    val max       = methodIn(apiModuleSymbol, "max")
    val read      = methodIn(apiModuleSymbol, "read")

    // matrix operators
    val transpose = matrixOp("t")
    val matmult   = matrixOp("%*%")
    val timesDouble     = matrixOp("*", Double)
    val divDouble       = matrixOp("/", Double)
    val plusDouble      = matrixOp("+", Double)
    val minusDouble     = matrixOp("-", Double)
    val timesMatrix     = matrixOp("*", Matrix)
    val divMatrix       = matrixOp("/", Matrix)
    val plusMatrix      = matrixOp("+", Matrix)
    val minusMatrix     = matrixOp("-", Matrix)

    val sourceOps   = Set(zeros, ones, rand, fromDataFrame)
    val builtinOps  = Set(sum, mean, max, read)
    val matOps      = Set(transpose, matmult, timesDouble, timesMatrix, divDouble, divMatrix, plusDouble, plusMatrix, minusDouble, minusMatrix)

    val ops: Set[u.MethodSymbol] = sourceOps | builtinOps | matOps

    val modules = Set(apiModuleSymbol, matrixModuleSymbol)

    // Set of valid inputs to the macro
    val inputs = Set(Matrix, DataFrame)
    val primitives = Set(Double, Int)


  }
}
