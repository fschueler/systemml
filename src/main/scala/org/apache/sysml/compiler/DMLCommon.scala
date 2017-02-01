package org.apache.sysml.compiler

import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.sysml.api.linalg.Matrix
import org.apache.sysml.api.mlcontext.MLContext
import org.emmalanguage.compiler.Common

trait DMLCommon extends Common {

  class Environment(val inputs: Map[String, u.TermSymbol],
                    val bindingRefs: Map[String, u.TermSymbol],
                    val offset: Int) {

  }

  // --------------------------------------------------------------------------
  // API
  // --------------------------------------------------------------------------
  import universe._

  override val rootPkg = "org.apache.sysml.api.linalg"

  protected[sysml] object DMLAPI extends IRModule {

    val predefModuleSymbol      = api.Sym[scala.Predef.type].asModule
    val apiModuleSymbol         = api.Sym[org.apache.sysml.api.linalg.api.`package`.type].asModule
    val matrixSymbol            = api.Sym[org.apache.sysml.api.linalg.Matrix].asClass
    val doubleSymbol            = api.Sym[scala.Double].asClass
    val intSymbol               = api.Sym[scala.Int].asClass
    val matrixModuleSymbol      = matrixSymbol.companion.asModule
    def module                  = apiModuleSymbol

    private def matrixOp(name: String) = methodIn(matrixSymbol, name)
    private def matrixOp(name: String, paramType: u.Type) = methodIn(matrixSymbol, name, paramType)

    private def doubleOp(name: String) = methodIn(doubleSymbol, name)
    private def doubleOp(name: String, paramType: u.Type) = methodIn(doubleSymbol, name, paramType)

    private def intOp(name: String) = methodIn(intSymbol, name)
    private def intOp(name: String, paramType: u.Type) = methodIn(intSymbol, name, paramType)

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
    val MLContext   = api.Type[MLContext].typeConstructor
    val Matrix      = api.Type[Matrix].typeConstructor
    val DataFrame   = api.Type[DataFrame].typeConstructor
    val Double      = api.Type[Double].typeConstructor
    val Int         = api.Type[Int].typeConstructor

    /**
      * This is a list of all supported operations both on primitives and our Matrix. There are things we can do and things
      * we can't do on primitives in DML.
      */

    // Sources
    val zeros               = methodIn(matrixModuleSymbol, "zeros")
    val ones                = methodIn(matrixModuleSymbol, "ones")
    val rand                = methodIn(matrixModuleSymbol, "rand")
    val fromDataFrame       = methodIn(matrixModuleSymbol, "fromDataFrame")

    // builtin functions
    val sum       = methodIn(apiModuleSymbol, "sum")
    val mean      = methodIn(apiModuleSymbol, "mean")
    val min       = methodIn(apiModuleSymbol, "min")
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

    // Double/Double  operators
    val plusDD    = doubleOp("+", Double)
    val minusDD   = doubleOp("-", Double)
    val timesDD   = doubleOp("*", Double)
    val divDD     = doubleOp("/", Double)
    val geqDD     = doubleOp(">=", Double)
    val leqDD     = doubleOp("<=", Double)
    val lessDD    = doubleOp("<", Double)
    val greaterDD = doubleOp(">", Double)

    // Int/Int operators
    val plusII    = intOp("+", Int)
    val timesII   = intOp("*", Int)
    val divII     = intOp("/", Int)
    val minusII   = intOp("-", Int)
    val geqII     = intOp(">=", Int)
    val leqII     = intOp("<=", Int)
    val lessII    = intOp("<", Int)
    val greaterII = intOp(">", Int)


    val sourceOps   = Set(zeros, ones, rand, fromDataFrame)
    val builtinOps  = Set(sum, mean, min, max, read)
    val matOps      = Set(transpose, matmult, timesDouble, timesMatrix, divDouble, divMatrix, plusDouble, plusMatrix, minusDouble, minusMatrix)
    val doubleOps   = Set(plusDD, minusDD, timesDD, divDD, geqDD, leqDD, lessDD, greaterDD)
    val intOps      = Set(plusII, minusII, timesII, divII, geqII, leqII, lessII, greaterII)

    val ops: Set[u.MethodSymbol] = sourceOps | builtinOps | matOps | doubleOps | intOps

    val modules = Set(apiModuleSymbol, matrixModuleSymbol, predefModuleSymbol)

    // Set of valid inputs to the macro
    val inputs = Set(Matrix, DataFrame, MLContext)
    val primitives = Set(Double, Int)


  }
}
