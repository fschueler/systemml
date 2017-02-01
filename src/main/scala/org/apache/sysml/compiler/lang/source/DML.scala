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
import org.apache.sysml.compiler.DMLCommon
import org.emmalanguage.compiler.Common
import org.emmalanguage.compiler.lang.source.Source

import scala.collection.mutable
import scala.util.Random

trait DML extends DMLCommon with DMLSourceValidate {
  this: Source =>

  object DML {

    lazy val toDML = (tree: u.Tree) => (env: Environment) =>  DMLTransform.generateDML(unQualifyStatics(tree), env)

    lazy val valid = DMLSourceValidate.valid

    lazy val validate = (tree: u.Tree) => valid(tree).isGood

    private[source] object DMLTransform {

      type D = Environment => String
      // semantic domain (env => string representation)
      val indent = 0

      // the last seen lhs of a valdef
      var currLhs: u.TermSymbol = _

      val generateDML: (u.Tree, Environment) => String = (tree: u.Tree, startingEnv: Environment) => {

        val ops = Set('=', '+', '-', '*', '/', '%', '<', '>', '&', '|', '!', '?', '^', '\\', '@', '#', '~')

        val booleanOps = Set(">", "<", "<=", ">=", "==", "!=")

        val matrixFuncs = Set("t", "nrow", "ncol")

        val constructors = Set("zeros", "rand", "ones")

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

        val printConstructor = (sym: u.MethodSymbol, argss: Seq[Seq[D]], env: Environment, isVector: Boolean) => {
          val args = argss flatMap (args => args map (arg => arg(env)))

          sym.name match {
            case u.TermName("rand")  => if (isVector) s"rand(rows=${args(0)}, cols=1)" else s"""rand(rows=${args(0)}, cols=${args(1)})"""
            case u.TermName("zeros") => if (isVector) s"matrix(0.0, rows=${args(0)}, cols=1)" else s"matrix(0.0, rows=${args(0)}, cols=${args(1)})"
            case u.TermName("ones")  => if (isVector) s"matrix(1.0, rows=${args(0)}, cols=1)" else s"matrix(1.0, rows=${args(0)}, cols=${args(1)})"
            case u.TermName("diag")  => s"diag(matrix(${args(0)}, rows=${args(1)}, cols=${args(1)}))"
            case u.TermName("apply") => s"matrix(${args(0)}, rows=${args(1)}, cols=${args(2)})"

            /* Here we just remove the call to Matrix.fromDataFrame(ref) with ref. We will take care of setting the input
               to the MLContext so that the actual dataframe reference will be passed with the name "ref"
             */
            case u.TermName("fromDataFrame") => args(0)
          }
        }

        val printBuiltin = (target: D, sym: u.MethodSymbol, argss: Seq[Seq[D]], env: Environment) => {
          val args = argss flatMap (args => args map (arg => arg(env)))

          sym.name match {
            case u.TermName("read") => {
              s"""read(${args(0)}, format="${args(1).toLowerCase()}")"""
            }

            case u.TermName("write") => {
              val format = args(2) match {
                case "CSV" => """format="csv""""
                case _ => throw new RuntimeException(s"Unsopported output format: ${args(2)}")
              }
              s"write(${args(0)}, ${args(1)}, $format)"
            }

            case u.TermName(fname) if fname == "MatrixOps" => args.head.toString

            case u.TermName(fname) => s"$fname(${args.mkString(", ")})"

            case _ =>
              abort(s"Unsopported builtin call: $sym", sym.pos)
          }
        }

        val isUnary = (sym: u.MethodSymbol) =>
          sym.name.decodedName.toString.startsWith("unary_")

        val printArgss = (argss: Seq[Seq[D]], env: Environment) =>
          (argss map (args => (args map (arg => arg(env))).mkString("(", ", ", ")"))).mkString

        val printParams = (params: Seq[D], env: Environment) =>
          (params map (param => param(env))).mkString("(", ", ", ")")

        val printParamss = (paramss: Seq[Seq[D]], env: Environment) =>
          (paramss map (params => printParams(params, env))).mkString

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

        // format a block to have 2 whitespace indentaion
        val indent = (str: String) => {
          val lines = str.split("\n")

          if (lines.length > 1)
            lines.map(x => s"  $x").mkString("\n")
          else
            s"  $str"
        }

        /**
          *constructs a for loop by deconstructing foreach together with the range and the lambda */
        val forLoop = (target: D, targs: Seq[u.Type], args: Seq[D], env: Environment) => {
          val range = target(env)
          val lambda = args.map(x => x(env)).head

          val parts = lambda.split(" => ")

          val idx = parts(0).drop(1).dropRight(1) // remove braces

          // format the body with 2 spaces of indentation
          val body = indent(parts(1))

          s"""
             |for ($idx in $range) {
             |$body
             |}
            """.stripMargin.trim
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

          def empty: D = env => ""

          // Atomics
          def lit(value: Any): D = env => value match {
            //@formatter:off
            case value: Boolean => if (value) "TRUE" else "FALSE"
            case value: Char => s""""${value}""""
            case value: String => s""""${escape(value)}""""
            case null => "null"
            case value: Unit => ""
            case _ => value.toString
            //@formatter:on
          }

          def this_(sym: u.Symbol): D = env => {
            s"_this: ${sym.name.decodedName.toString}"
          }

          def bindingDef(lhs: u.TermSymbol, rhs: D): D = valDef(lhs, rhs)

          def ref(target: u.TermSymbol): D = ???

          def loop(cond: D, body: D): D = whileLoop(cond, body)

          def moduleAcc(target: D, member: u.ModuleSymbol): D = ???

          override def bindingRef(sym: u.TermSymbol): D = env => {
            printSym(sym)
          }

          override def moduleRef(target: u.ModuleSymbol): D = env =>
            printSym(target)

          // Definitions
          override def valDef(lhs: u.TermSymbol, rhs: D): D = env =>
            s"${printSym(lhs)} = ${rhs(env)}"

          override def parDef(lhs: u.TermSymbol, rhs: D): D = env => {
            val l = lhs.name.decodedName
            val tpe = lhs.typeSignature
            val r = rhs(env)

            if (r.isEmpty) {
              s"$l"
            } else {
              s"$l = $r"
            }
          }

//          def defDef(sym: u.MethodSymbol, tparams: Seq[u.TypeSymbol], paramss: Seq[Seq[D]], body: D): D = env => {
//            if (tparams.length > 0) {
//              abort(s"Type parameters are not supported: ${tparams}")
//            }
//
//            udfRegistry.add(sym)
//
//            val name = sym.name.decodedName
//            val block = body(env)
//            val statements = indent(block.split("\n").dropRight(1).mkString("\n")) // drop last line
//            val expression = block.split("\n").takeRight(1).head
//
//            val (inT, outT) = sym.typeSignature match { case u.MethodType(params, resulttype) => (params.map(_.typeSignature), List(resulttype))}
//
//            // transform scala types to systemml types
//            val inputTypes = inT.flatMap(convertTypes(_))
//            val inputNames = paramss.flatten.map(_(env))
//
//            val outputType = outT.flatMap(convertTypes(_))
//            if (outputType.length > 1) {
//              abort(s"Currently we only support function definitions with a single return value but we found ${outputType.length} values of type $outT")
//            }
//
//            // TODO enable multiple return values
//            // val rng = new Random()
//            // val outputNames = List.fill(outputType.length)("_$x" + rng.nextString(3))
//
//            val inputs = inputTypes.zip(inputNames).map(tup => s"${tup._1} ${tup._2}").mkString(", ")
//            val outputs = s"${outputType.head} x99"
//
//            if (statements != "  ") {
//              s"""
//                 |$name = function($inputs) return ($outputs) {
//                 |$statements
//                 |  x99 = $expression
//                 |}
//             """.stripMargin.trim
//            } else {
//              s"""
//                 |$name = function($inputs) return ($outputs) {
//                 |  x99 = $expression
//                 |}
//             """.stripMargin.trim
//            }
//          }

          // Other

          /** type ascriptions such as `var x: Int` */
          def typeAscr(target: D, tpe: u.Type): D = env => "" // TODO check for types allowed in SystemML

          def defCall(target: Option[D], method: u.MethodSymbol, targs: Seq[u.Type], argss: Seq[Seq[D]]): D = env => {
            val s = target
            val args = argss flatMap (args => args map (arg => arg(env)))

            (target, argss) match {

              /* matches tuples */
              case (Some(tgt), _) if isApply(method) && api.Sym.tuples.contains(method.owner.companion) =>
                ""

              case (Some(tgt), _) if method == api.Sym.foreach || method.overrides.contains(api.Sym.foreach) =>
                forLoop(tgt, targs, argss.flatten, env)

              /* matches unary methods without arguments, e.g. A.t */
              case (Some(tgt), Nil) if isUnary(method) =>
                s"${printSym(method)}${tgt(env)}"

              /* matches apply methods with one argument */
              case (Some(tgt), ((arg :: Nil) :: Nil)) if isApply(method) =>
                s"${tgt(env)}${printMethod(" ", method, " ")}${arg(env)}"

              /* matches methods with one argument (%*%, read) */
              case (Some(tgt), (arg :: Nil) :: Nil) => {

                val module = tgt(env)

                // methods in the package object (builtins with one argument (read)
                if (module == "package") {
                  printBuiltin(tgt, method, argss, env)
                }

                // matrix constructors with one argument (fromDataFrame)
                else if (module == "Matrix") {
                  printConstructor(method, argss, env, false)
                }

                else if (module == "Vector") {
                  printConstructor(method, argss, env, true)
                }

                // methods from scala.predef with one argument (println(...) etc.)
                else if (module == "Predef") {
                  val name = method.name.decodedName

                  name match {
                    case u.TermName("println") => s"print(${arg(env)})"
                    case u.TermName("intWrapper") => s"${arg(env)}"
                    case _ => abort(s"scala.predef.$name is not supported in DML", method.pos)
                  }
                }

                // binary operators
                else {
                  method.name.decodedName match {
                    case u.TermName("to") | u.TermName("until") => s"${tgt(env)}:${arg(env)}"
                    case u.TermName("%") => s"($module %% ${args(0)})" // modulo in dml is %%
                    case u.TermName("&&") => s"($module & ${args(0)})" // && in dml is &
                    case u.TermName("||") => s"($module | ${args(0)})" // || in dml is |
                    case _ => s"($module ${method.name.decodedName} ${args(0)})"
                  }
                }
              }

              // matches apply methods with multiple arguments
              case (Some(tgt), (x :: xs) :: Nil) if isApply(method) => {
                val module = tgt(env)
                val argString = args.mkString(" ")

                if (module == "Seq" || module == "Array") {
                  // sequence/array constructors
                  s""""$argString""""
                }

                else if (module == "Matrix") {
                  printConstructor(method, argss, env, false)
                }

                else if (module == "Vector") {
                  printConstructor(method, argss, env, true)
                }

                else if (module == "package") {
                  // builtins
                  "builtin"
                }

                else {
                  val r = args(0) // rows
                  val c = args(1) // columns

                  if (c == ":::")
                    s"$module[$r + 1,]"
                  else if (r == ":::")
                    s"$module[,$c + 1]"
                  else
                    s"as.scalar($module[$r + 1,$c + 1])"
                }

//                else
//                  "case (Some(tgt), (x :: xs) :: Nil) if isApply(method)"
              }

              // matches apply methods with multiple arguments
              case (Some(tgt), (x :: xs) :: Nil) if isUpdate(method) => {
                val module = tgt(env)

//                if (bindingRefs.contains(module)) {
                  // update on matrix objects (left indexing): A[r, c] = v === A.update(r, c, v)
                  val r = args(0) // rows
                  val c = args(1) // columns
                  val v = args(2) // value to update with

                  if (c == ":::")
                    s"$module[$r + 1,] = $v"
                  else if (r == ":::")
                    s"$module[,$c + 1] = $v"
                  else
                    s"$module[$r + 1,$c + 1] = $v"
//                }
//                else
//                  "case (Some(tgt), (x :: xs) :: Nil) if isUpdate(method)"
              }

              // matches methods with multiple arguments (e.g. zeros(3, 3), write)
              case (Some(tgt), (x :: xs) :: Nil) => {

                val module = tgt(env)
                val argString = args.mkString(" ")

                if (module == "Matrix") {
                  printConstructor(method, argss, env, false)
                }

                else if (module == "Vector") {
                  printConstructor(method, argss, env, true)
                }

                else if (module == "package") {
                  // builtin
                  printBuiltin(tgt, method, argss, env)
                }

                else {
                  "case (Some(tgt), (x :: xs) :: Nil)"
                }
              }

              // matches functions without arguments (.t (transpose))
              case (Some(tgt), Nil) => {

                method.name.decodedName match {
                  case u.TermName(tn) if matrixFuncs.contains(tn) => s"$tn(${tgt(env)})"
                  case u.TermName(tn) if tn == "toDouble" => tgt(env) // this is a scala implicit conversion from Int to Double
                  // case u.TermName(tn) if Seq("$line", "$read", "$iw", "INSTANCE").exists(tn.contains(_)) => tgt(env) // this is the case for accesses to vars and vals in the REPL/interpreter (classbased)
                  case _ => method.name.decodedName.toString // here we catch references to outside values that might have names such as outer1.outer2.x
                }
              }

              case (Some(tgt), _) => abort(s"Matching error, please report the following: case case (Some(tgt), _): target ${tgt(env)}")

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

          def inst(target: u.Type, targs: Seq[u.Type], argss: Seq[Seq[D]]): D = ???

          def lambda(sym: u.TermSymbol, params: Seq[D], body: D): D = env => {
            val p = params.map(p => p(env))
            val b = body(env)

            s"""(${p.mkString(", ")}) => $b"""
          }

          def branch(cond: D, thn: D, els: D): D = env => {
            val thenBlock = indent(thn(env))
            val elseBlock = indent(els(env))

            if (elseBlock != "  ") {
              s"""
                 |if (${cond(env)}) {
                 |$thenBlock
                 |} else {
                 |$elseBlock
                 |}
            """.stripMargin.trim
            } else {
              s"""
                 |if (${cond(env)}) {
                 |$thenBlock
                 |}
            """.stripMargin.trim
            }
          }

          def block(stats: Seq[D], expr: D): D = env => {
            val statsString = stats.flatMap{x =>
              val res = x(env)
              if (env.bindingRefs.keySet.contains(res)) {
                None // if the statement is a single varref/valref, remove it
              } else {
                Some(res)
              }
            }.filter(x => x.trim.length > 0).mkString("\n")

            val exprString  = expr(env)

            // if the expression is a valref or varref, we leave it out because SystemML doesn't allow single expressions as statements
            val returnExpr = if (env.bindingRefs.keySet.contains(exprString)) {
              ""
            } else {
              exprString
            }

            s"""
               |$statsString
               |$returnExpr
            """.stripMargin.trim
          }

          def whileLoop(cond: D, body: D): D = env => {
            val formatted = indent(body(env))

            s"""
               |while(${cond(env)}) {
               |$formatted
               |}
             """.stripMargin.trim
          }

          def varMut(lhs: u.TermSymbol, rhs: D): D = env => {
            s"${lhs.name.decodedName} = ${rhs(env)}"
          }
        }
        Source.fold(alg)(tree)(startingEnv)
      }
    }
  }
}
