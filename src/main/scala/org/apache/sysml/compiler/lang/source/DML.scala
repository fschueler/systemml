/*
 * Copyright © 2014 TU Berlin (emma@dima.tu-berlin.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sysml.compiler.lang.source

import org.apache.sysml.api.linalg.Matrix
import org.emmalanguage.compiler.Common
import org.emmalanguage.compiler.lang.core.Core
import org.emmalanguage.compiler.lang.source.Source

import scala.collection.mutable
import scala.util.Random

trait DML extends Common {
  this: Source =>

  object DML {

    lazy val toDML = unQualifyStatics andThen DMLTransform.generateDML

    def sources = DMLTransform.sources

    private[source] object DMLTransform {

      type D = Int => String
      // semantic domain (offset => string representation)
      val indent = 0

      /** contains all sources that have to be specified in the MLContext
        * with their names and the names in the dml script) */
      val sources: mutable.Set[(String, u.TermSymbol)] = mutable.Set.empty

      // map of all binding references to get the symbol for dataframes that are passed from outside the macro
      val bindingRefs: mutable.Map[String, u.TermSymbol] = mutable.Map.empty

      // the last seen lhs of a valdef
      var currLhs: u.TermSymbol = _

      val generateDML: u.Tree => String = (tree: u.Tree) => {

        val ops = Set('=', '+', '-', '*', '/', '%', '<', '>', '&', '|', '!', '?', '^', '\\', '@', '#', '~')

        val matrixOps = Set("%*%")

        val matrixFuncs = Set("t", "nrow", "ncol")

        val constructors = Set("zeros", "rand")

        val builtins = Set("read", "write", "min", "max", "mean", "sum")

        val printSym = (sym: u.Symbol) => {
          val decName = sym.name.decodedName.toString.stripPrefix("unary_")
          val encName = sym.name.encodedName.toString.stripPrefix("unary_")

          decName
          // TODO we have to make sure to transform invalid references into valid ones or raise an exception
          //        if (decName == encName && !kws.contains(decName) || sym.isMethod && decName.forall(ops.contains)) decName
          //        else s"`$decName`"
        }

        val isConstructor = (sym: u.MethodSymbol) =>
          constructors.contains(sym.name.toString)

        val isMatrixOp = (sym: u.MethodSymbol) => {
          val s = sym.name.decodedName.toString
          matrixOps.contains(s)
        }

        val isBuiltin = (sym: u.MethodSymbol) => {
          val s = sym.name.decodedName.toString
          builtins.contains(s)
        }

        val isApply = (sym: u.MethodSymbol) =>
          sym.name == u.TermName("apply")

        val isUpdate = (sym: u.MethodSymbol) =>
          sym.name == u.TermName("update")

        val printMethod = (pre: String, sym: u.MethodSymbol, suf: String) =>
          if (isApply(sym)) ""
          else pre + printSym(sym) + suf

        val printConstructor = (sym: u.MethodSymbol, argss: Seq[Seq[D]], offset: Int, isVector: Boolean) => {
          val args = argss flatMap (args => args map (arg => arg(offset)))

          sym.name match {
            case u.TermName("rand")  => if (isVector) s"rand(rows=${args(0)}, cols=1)" else s"rand(rows=${args(0)}, cols=${args(1)})"
            case u.TermName("zeros") => if (isVector) s"matrix(0, rows=${args(0)}, cols=1)" else s"matrix(0, rows=${args(0)}, cols=${args(1)})"
            case u.TermName("ones")  => if (isVector) s"matrix(1, rows=${args(0)}, cols=1)" else s"matrix(1, rows=${args(0)}, cols=${args(1)})"
            case u.TermName("diag")  => s"diag(matrix(${args(0)}, rows=${args(1)}, cols=${args(1)}))"
            case u.TermName("apply") => s"matrix(${args(0)}, rows=${args(1)}, cols=${args(2)})"

            case u.TermName("fromDataFrame") => {
              // get the name of the referenced variable (dataframe)
              val in = bindingRefs.get(args.head) match {
                case Some(termSym) => termSym
                case _ =>
                  abort(s"Could not find reference to variable ${args.head} in Matrix.fromDataFrame", sym.pos)
              }
              // memoize the pair of (lhs, reference name)
              sources.add((currLhs.name.decodedName.toString, in))
              // the original valdef is removed as references to the variable will be resolved from MLContext
              "null"
            }
          }
        }

        val printMatrixOp = (target: D, sym: u.MethodSymbol, argss: Seq[Seq[D]], offset: Int) => {
          "A %*% B"
        }

        val printBuiltin = (target: D, sym: u.MethodSymbol, argss: Seq[Seq[D]], offset: Int) => {
          val args = argss flatMap (args => args map (arg => arg(offset)))

          sym.name match {
            case u.TermName("read") => {
              s"read(${args(0)})"
            }

            case u.TermName("write") => {
              val format = args(2) match {
                case "CSV" => """format="csv""""
                case _ => throw new RuntimeException(s"Unsopported output format: ${args(2)}")
              }
              s"write(${args(0)}, ${args(1)}, $format)"
            }

            case u.TermName(fname) => s"$fname(${args.mkString(", ")})"

            case _ =>
              abort(s"Unsopported builtin call: $sym", sym.pos)
          }
        }

        val isUnary = (sym: u.MethodSymbol) =>
          sym.name.decodedName.toString.startsWith("unary_")

        val printArgss = (argss: Seq[Seq[D]], offset: Int) =>
          (argss map (args => (args map (arg => arg(offset))).mkString("(", ", ", ")"))).mkString

        val printParams = (params: Seq[D], offset: Int) =>
          (params map (param => param(offset))).mkString("(", ", ", ")")

        val printParamss = (paramss: Seq[Seq[D]], offset: Int) =>
          (paramss map (params => printParams(params, offset))).mkString

        def printTpe(tpe: u.Type): String = {
          val tpeCons = tpe.typeConstructor
          val tpeArgs = tpe.typeArgs
          if (api.Sym.tuples contains tpeCons.typeSymbol) /* tuple type */ {
            (tpe.typeArgs map printTpe).mkString("(", ", ", ")")
          } else if (api.Sym.fun(Math.max(0, tpeArgs.size - 1)) == tpeCons.typeSymbol) /* function type */ {
            s"(${(tpe.typeArgs.init map printTpe).mkString(", ")}) => ${printTpe(tpe.typeArgs.last)}"
          } else if (tpeArgs.nonEmpty) /* applied higher-order type */ {
            s"${printSym(tpeCons.typeSymbol)}[${(tpe.typeArgs map printTpe).mkString(", ")}]"
          } else /* simple type */ {
            printSym(tpeCons.typeSymbol)
          }
        }


        /**
          * Convert Scala types to SystemML types
          * @param arg the type argument
          * @return DML type as string
          */
        def convertTypes(arg: u.Type): List[String] = arg match {
          case tpe if tpe =:= u.typeOf[Double] => List("double")
          case tpe if tpe =:= u.typeOf[Matrix] => List("matrix[double]")
          case tpe if tpe <:< u.typeOf[Product] => {
            val params = tpe.finalResultType.typeArgs
            params.flatMap(convertTypes(_))
          }
          case tpe => abort(s"Unsupported return type $tpe. Supported types are: Matrix, Double")
        }

        val escape = (str: String) => str
          .replace("\b", "\\b")
          .replace("\n", "\\n")
          .replace("\t", "\\t")
          .replace("\r", "\\r")
          .replace("\f", "\\f")
          .replace("\"", "\\\"")
          .replace("\\", "\\\\")

        val alg = new Source.Algebra[D] {

          def empty: D = offset => ""

          // Atomics
          def lit(value: Any): D = offset => value match {
            //@formatter:off
            case value: Char => s""""${value}""""
            case value: String => s""""${escape(value)}""""
            case null => "null"
            case value: Unit => ""
            case _ => value.toString
            //@formatter:on
          }

          def this_(sym: u.Symbol): D = ???

          def bindingRef(sym: u.TermSymbol): D = offset => {
            bindingRefs.put(sym.name.decodedName.toString, sym)
            printSym(sym)
          }

          def moduleRef(target: u.ModuleSymbol): D = offset =>
            printSym(target)

          // Definitions
          def valDef(lhs: u.TermSymbol, rhs: D): D = offset => {
            currLhs = lhs
            val rhsString = rhs(offset)
            /* if we have a reference to a dataframe (rhs == null),
             we need to scratch it and put the reference as input to mlContext */
            if (rhsString != "null")
              s"${printSym(lhs)} = $rhsString"
            else
              ""
          }

          def parDef(lhs: u.TermSymbol, rhs: D): D = offset => {
            val l = lhs.name.decodedName
            val tpe = lhs.typeSignature
            val r = rhs(offset)

            if (r.isEmpty) {
              s"$l"
            } else {
              s"$l = $r"
            }
          }

          def defDef(sym: u.MethodSymbol, tparams: S[u.TypeSymbol], paramss: SS[D], body: D): D = offset => {
            if (tparams.length > 0) {
              abort(s"Type parameters are not supported: ${tparams}")
            }

            val name = sym.name.decodedName
            val block = body(offset)
            val statements = block.split("\n").dropRight(1).mkString("\n")
            val expression = block.split("\n").takeRight(1).head

            val (inT, outT) = sym.typeSignature match { case u.MethodType(params, resulttype) => (params.map(_.typeSignature), List(resulttype))}

            // transform scala types to systemml types
            val inputTypes = inT.flatMap(convertTypes(_))
            val inputNames = paramss.flatten.map(_(offset))

            val outputType = outT.flatMap(convertTypes(_))
            if (outputType.length > 1) {
              abort(s"Currently we only support function definitions with a single return value but we found ${outputType.length} values of type $outT")
            }

            // TODO enable multiple return values
            // val rng = new Random()
            // val outputNames = List.fill(outputType.length)("_$x" + rng.nextString(3))

            val inputs = inputTypes.zip(inputNames).map(tup => s"${tup._1} ${tup._2}").mkString(", ")
            val outputs = s"${outputType.head} x99"

            val fn = s"""
             |$name = function($inputs) return ($outputs){
             |$statements
             |x99 = $expression
             |}
             """.stripMargin.trim

            fn
          }

          // Other

          /** type ascriptions such as `var x: Int` */
          def typeAscr(target: D, tpe: u.Type): D = offset => "" // TODO check for types allowed in SystemML

          def defCall(target: Option[D], method: u.MethodSymbol, targs: S[u.Type], argss: SS[D]): D = offset => {
            val s = target
            val args = argss flatMap (args => args map (arg => arg(offset)))
            (target, argss) match {

              /* matches tuples */
              case (Some(tgt), _) if isApply(method) && api.Sym.tuples.contains(method.owner.companion) =>
                ""

              /* matches unary methods without arguments, e.g. A.t */
              case (Some(tgt), Nil) if isUnary(method) =>
                s"${printSym(method)}${tgt(offset)}"

              /* matches apply methods with one argument */
              case (Some(tgt), ((arg :: Nil) :: Nil)) if isApply(method) =>
                s"${tgt(offset)}${printMethod(" ", method, " ")}${arg(offset)}"

              /* matches methods with one argument (%*%, read) */
              case (Some(tgt), (arg :: Nil) :: Nil) => {
                val module = tgt(offset)

                // methods in the package object (builtins with one argument (read)
                if (module == "package") {
                  printBuiltin(tgt, method, argss, offset)
                }

                // matrix constructors with one argument (fromDataFrame)
                else if (module == "Matrix") {
                  printConstructor(method, argss, offset, false)
                }

                else if (module == "Vector") {
                  printConstructor(method, argss, offset, true)
                }

                // methods from scala.predef with one argument (println(...) etc.)
                else if (module == "Predef") {
                  val name = method.name.decodedName

                  name match {
                    case u.TermName("println") => s"print(${arg(offset)})"
                    case u.TermName("intWrapper") => s"${arg(offset)}"
                    case _ => abort(s"scala.predef.$name is not supported in DML", method.pos)
                  }
                }

                // binary operators
                else {
                  method.name.decodedName match {
                    case u.TermName("to") | u.TermName("until") => s"${tgt(offset)}:${arg(offset)}"
                    //                  case u.TermName("foreach") => {
                    //                    constructForLoop(tgt, method, arg, offset)
                    //                  }
                    case _ => s"($module ${method.name.decodedName} ${args(0)})"
                  }
                }
              }

              // matches apply methods with multiple arguments
              case (Some(tgt), (x :: xs) :: Nil) if isApply(method) => {
                val module = tgt(offset)
                val argString = args.mkString(" ")

                if (module == "Seq" || module == "Array") {
                  // sequence/array constructors
                  s""""$argString""""
                }

                else if (module == "Matrix") {
                  printConstructor(method, argss, offset, false)
                }

                else if (module == "Vector") {
                  printConstructor(method, argss, offset, true)
                }

                else if (module == "package") {
                  // builtins
                  "builtin"
                }

                else if (bindingRefs.contains(module)) {
                  // apply on matrix objects (right indexing)
                  val Array(r, c) = argString.split(" ")
                  if (c == ":::")
                    s"$module[$r,]"
                  else if (r == ":::")
                    s"$module[,$c]"
                  else
                    s"$module[$r,$c]"
                }

                else
                  "case (Some(tgt), (x :: xs) :: Nil) if isApply(method)"
              }

              // matches apply methods with multiple arguments
              case (Some(tgt), (x :: xs) :: Nil) if isUpdate(method) => {
                val module = tgt(offset)
                val argString = args.mkString(" ")

                if (bindingRefs.contains(module)) {
                  // apply on matrix objects (right indexing)
                  val Array(r, c, v) = argString.split(" ")
                  if (c == ":::")
                    s"$module[$r,] = $v"
                  else if (r == ":::")
                    s"$module[,$c] = $v"
                  else
                    s"$module[$r,$c] = $v"
                }

                else
                  "case (Some(tgt), (x :: xs) :: Nil) if isUpdate(method)"
              }

              // matches methods with multiple arguments (e.g. zeros(3, 3), write)
              case (Some(tgt), (x :: xs) :: Nil) => {

                val module = tgt(offset)
                val argString = args.mkString(" ")

                if (module == "Matrix") {
                  printConstructor(method, argss, offset, false)
                }

                else if (module == "Vector") {
                  printConstructor(method, argss, offset, true)
                }

                else if (module == "package") {
                  // builtin
                  printBuiltin(tgt, method, argss, offset)
                }

                else {
                  "case (Some(tgt), (x :: xs) :: Nil)"
                }
              }

              // matches functions without arguments (.t (transpose))
              case (Some(tgt), Nil) => {
                method.name.decodedName match {
                  case u.TermName(tn) if matrixFuncs.contains(tn) => s"$tn(${tgt(offset)})"
                  case _ =>
                    "case (Some(tgt), Nil)"
                }
              }

              case (Some(tgt), _) => {
                s"case (Some(tgt), _)"
              }

                // matches functions that are not defined in a module or class (udfs)
              case (None, _) => {
                val name = method.name.decodedName
                val argString = args.mkString(", ")
                s"$name($argString)"
              }

              case _ =>
                abort(s"Unsupported function call! Calling the method $method is not supported!")
            }
          }

          def inst(target: u.Type, targs: Seq[u.Type], argss: SS[D]): D = ???

          def lambda(sym: u.TermSymbol, params: S[D], body: D): D = offset => {
            val p = params.map(p => p(offset))
            val b = body(offset)

            s"""(${p.mkString(", ")}) => $b"""
          }

          def branch(cond: D, thn: D, els: D): D = offset => {
            s"""
              |if (${cond(offset)}) {
              |${thn(offset)}
              |} else {
              |${els(offset)}
              |}
            """.
              stripMargin.trim
          }

          def block(stats: S[D], expr: D): D = offset => {
            val statsString = stats.flatMap{x =>
              val res = x(offset)
              if (bindingRefs.keySet.contains(res)) {
                None // if the statement is a single varref/valref, remove it
              } else {
                Some(res.trim.replaceAll("\n", ""))
              }
            }.mkString("\n")

            val exprString  = expr(offset)

            // if the expression is a valref or varref, we leave it out because SystemML doesn't allow single expressions as statements
            val returnExpr = if (bindingRefs.keySet.contains(exprString)) {
              ""
            } else {
              exprString
            }

            val resString =
              s"""
                 |$statsString
                 |$returnExpr
            """.
                stripMargin.trim

            resString
          }

          def whileLoop(cond: D, body: D) = offset => {
            s"""
               |while(${cond(offset)}) {
               |${body(offset)}
               |}
             """.stripMargin.trim
          }

          /**
            *constructs a for loop by deconstructing foreach together with the range and the lambda */
          def forLoop(target: D, targs: S[u.Type], args: S[D]): D = offset => {
            val range = target(offset)
            val lambda = args.map(x => x(offset)).head

            val parts = lambda.split(" => ")

            val idx = parts(0).drop(1).dropRight(1) // remove braces

            val body = parts(1).stripMargin.trim

            val loop =
              s"""
              |for ($idx in $range) {
              |$body
              |}
            """.stripMargin.trim

            loop
          }

          def varMut(lhs: u.TermSymbol, rhs: D): D = offset => {
            s"""
               |${lhs.name.decodedName} = ${rhs(offset)}
           """.stripMargin
          }
        }
        Source.fold(alg)(tree)(0)
      }
    }
  }
}
