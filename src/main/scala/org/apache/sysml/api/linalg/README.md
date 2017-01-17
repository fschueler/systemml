# SystemML Scala DSL
Developer Documentation

## Overview

The SystemML Scala DSL enables data scientists to write algorithms for SystemML
using Scala syntax with specific `Matrix` and `Vector` types. These types come with the promise that every operation on them will be executed using the SystemML compiler and runtime. The DSL is based on a quotation approach but will be further developed to enable eager or lazily evaluated linear algebra expressions. Due to the constraints of SystemML and using its language (DML) as a target language, we do not support the full set of Scala expressions in the current DSL. After reading through this documentation you should know what is and what isn't supported.

to get you motivated, here is a first example of an algorithm written in DML and executed using the `MLContext` compared to the same algorithm written and executed in Scala:

<head>
<title>HTML Table Header</title>
</head>
<body>
<table border="1" width="100%">
<caption>This is the caption</caption><tr>
<th>DML</th>
<th>Scala</th>
</tr>
<tr>
<td>
<pre lang="R">
# tfidf feature matrix coming from somewhere
tfidf = "1.0, 2.0, 3.0, 4.0"
k = 40
m = 2
n = 2
maxIters = 200

# initialize matrices
V = matrix(tfidf, rows=m, cols=n)
W = rand(rows=m, cols=k)
H = rand(rows=k, cols=n)

for (i in 0:maxIters) {
  H = ((H * (t(W) %*% V)) / (t(W) %*% (W %*% H)))
  W = ((W * (V %*% t(H))) / (W %*% (H %*% t(H))))
}
</pre>
</td>
<td>
<pre lang="scala">
// tfidf feature matrix coming from somewhere
val tfidf = Array(1.0, 2.0, 3.0, 4.0)
val k = 40
val m, n = 2 // dimensions of tfidf
val maxIters = 200

// initialize matrices
val V = Matrix(tfidf, m, n)
var W = Matrix.rand(m, k)
var H = Matrix.rand(k, n)

for (i <- 0 to maxIters) { //main loop
  H = H * (W.t %*% V) / (W.t %*% (W %*% H))
  W = W * (V %*% H.t) / (W %*% (H %*% H.t))
}
</pre>
</td>
</tr>
</table>
</body>

As an overwiew, we collected the main differences between DML and our Scala DSL in the following table. Since the error- and debug facilities of SystemML and our Scala DSL are still in their infancy, the best way of debugging right now is to look at the generated DML code and plan of instructions that are printed before executing a DSL written algorithm.

Main differences between the Scala DSL and DML. Notice that these are only important for debugging purposes. To write an algorihm in the Scala DSL you should always use the Scala way just like you would for your other Scala programs, e.g. think 0-indexing and write `&&` for `AND`. The conversion is done in the compiler and these differences are just for relating generated to written code.

|Feature          |     DML       |   Scala DSL    |
| :---            |    :---:      |     :---:      |
| Array-indexing  |  1-based      | 0-based        |
| Logical `AND`   |    `&`        | `&&`           |
| Logical `OR`    |      `|`      |   `||`     |

### Installation

To install the Scala DSL we currenlty require a custom version of the [Emma](https://github.com/emmalanguage/emma) framework. After stabilizing the DSL these changes will be merged into the Emma repository and distributed on maven central.

```bash
git clone git@github.com:fschueler/emma.git
cd emma
git checkout newir
mvn clean install -DskipTests -pl emma-language
```

Now we can download the SystemML sources (make sure to leave the emma directory).

```bash
git clone git@github.com:fschueler/incubator-systemml.git
```

Import the project into an IDE of your choice (as maven project) and write an Algorithm similar to the one in `incubator-systemml/src/test/scala/org/apache/sysml/examples/TSNE.scala`.

## Getting Started

The current API to write user code for SystemML includes a `Matrix` and `Vector` type, where vector is just syntactic sugar for a column matrix with a single column in SystemML.
Additionally, we aim to provide all builtin functions that SystemML currently supports. The current state of the DSL is a first alpha and many things are still missing. If you encounter problems or missing features, please add it to the list [here](https://issues.apache.org/jira/browse/SYSTEMML)

### Algorithm structure

Algorithms in the Scala DSL are implemented as arguments to the macro `parallelize` which works just like a Scala function and takes a Scala expression as input. The simplest setup would be in a Scala `App` object as in:

```scala
package org.apache.sysml.examples

import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api._

object MyAlgorithm extends App {

val algorithm = parallelize {
    // code written here will be executed in SystemML
  }
}
```

`App` objects in Scala don't need a `main`-method and can be directly executed from within the IDE. The function `parallelize` takes our Scala code and converts it into DML code. It then wraps everything inside an instance of the class `SystemMLAlgorithm[T]` and returns it. The type parameter `T` will be the type of the returned values inside the `parallelize` block. To make it more concrete, let's consider a simple example. To be able to run the algorithm on Spark we also have to create a `SparkContext` and initialize the `MLContext` to be able to communicate with SystemML.

```scala
package org.apache.sysml.examples

import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api._

object MyAlgorithm extends App {

val conf = new SparkConf()
                .setMaster("local[2]")
                .setAppName("SystemML Spark App")

val sc: SparkContext = new SparkContext(conf)
val ml: MLContext    = new MLContext(sc)

val algorithm: SystemMLAlgorithm[Matrix] = parallelize {
  val A: Matrix = Matrix.rand(5, 3)
  val B: Matrix = Matrix.rand(3, 7)
  val C: Matrix = A %*% B
  C
  }
}
```

This code performs a simple matrix multiplication of two random matrices and returns the result in the variable `C`. The result of `parallelize` is then `SystemMLAlgorithm[Matrix]` which is assigned to the value `algorithm`. Up until now, no code has actually been executed. To run the algorithm with SystemML on Spark we have to evoke the `run()` method on the algorithm instance:

```scala
package org.apache.sysml.examples

import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api._

object MyAlgorithm extends App {

  val conf = new SparkConf()
                  .setMaster("local[2]")
                  .setAppName("SystemML Spark App")

  val sc: SparkContext = new SparkContext(conf)
  val ml: MLContext    = new MLContext(sc)

  val algorithm: SystemMLAlgorithm[Matrix] = parallelize {
    val A: Matrix = Matrix.rand(5, 3)
    val B: Matrix = Matrix.rand(3, 7)
    val C: Matrix = A %*% B
    C
  }

  val result: Matrix = algorithm.run()
}
```

This will execute the algorithm using SystemML and Spark. Executing the `run()`-method then returns the result of type `Matrix`.

Internally, the `parallelize` function converts the Scala code that it gets as an argument into DML. For our above example, the expanded/transformed version would look like this:

```Scala
val algorithm = new SystemMLAlgorithm[Matrix]  {

  def run(): Matrix = {
    val dmlString = s"""| A = rand(5, 3)
                        | B = rand(3, 7)
                        | C = A %*% B
                    """

    val ml = implicitly[MLContext]              
    val script = dml(dmlString).in(Seq()).out(Seq("C"))
    val res = ml.execute(script)
    val out = res.getTuple[Matrix]("C")
    out._1
  }

  val result: Matrix = algorithm.run()
}
```

### Error handling

Before execution, we print the generated DML code including line-numbers and the plan of generated instructions. Error-messages returned from SystemML refer to the line-numbers in generated DML while errors thrown during translation can have different causes and therefore different appearances. We're working on improved error handling in both cases. If you get a message that says `Error during macro expansion...` then it's probably not your fault and something inside the translation phase went wrong. In this case, please open a new Jira issue with your code and the error message.

If the error comes from SystemML, it could be that there is an error in the generated DML code or that there is a semantic error in your code. In both cases feel free to report the error or ask on our [Mailing List]() for help.


### Implementation

The translation of Scala code into DML is realized by using the [Emma compilation framework]() which uses Scala macros to allow for modifications of Scala code. The input code is represented as abstract syntax tree (AST) and different transformations facilitate the analysis and transformation of this AST. Finally, we map the nodes of the Scala AST (such as value-definitions and function applications) to the corresponding constructs in DML. Using macros allows for a holistic view of the program and potentially even for efficient optimizations. One complication is that Scala is based on expressions while SystemML DML is focused on statements. This requires some transformation and analysis of the Scala AST to transform expressions into statements. For example the above snippet that shows the generated DML code for the matrix multiplication lacks the "C" as compared to the Scala code. We remove this variable in the DML code because the SystemML parser does not allow for single expression statements. At the same time, we have to remember the name and type of "C" to later retain it from the `MLContext` after execution and to avoid typecheck errors due to wront return values.

One of the strengths of this approach is that while we write our programs we have the full guarantee of type-safety and support from our IDE. Additionally, the AST includes control-flow structures such as `for`-loops and conditional `if-then-else` expressions that can be translated natively into DML. This allows for efficient execution and full optimization potential from SystemML's internal optimizer.

##A larger example

As an example that involves control flow (the for-loop) we show how the NMF algorithm can be implemented in the Scala DSL:

```Scala
import org.apache.sysml.api.linalg._
import org.apache.sysml.api.linalg.api._

object MyApp extends App {

  val conf = new SparkConf()
                  .setMaster("local[2]")
                  .setAppName("SystemML Spark App")

  val sc: SparkContext = new SparkContext(conf)
  val ml: MLContext    = new MLContext(sc)

  val nmf = parallelize {
    // tfidf feature matrix coming from somewhere
    val tfidf = Array(1.0, 2.0, 3.0, 4.0)
    val k = 40
    val m, n = 2 // dimensions of tfidf
    val maxIters = 200

    // initialize matrices
    val V = Matrix(tfidf, m, n)
    var W = Matrix.rand(m, k)
    var H = Matrix.rand(k, n)

    for (i <- 0 to maxIters) { //main loop
      H = H * (W.t %*% V) / (W.t %*% (W %*% H))
      W = W * (V %*% H.t) / (W %*% (H %*% H.t))
    }

    (W, H) // return values
  }

  val (w, h) = nmf.run()
}
```
We write the whole algorithm inside the `parallelize` block and specify our return value at the end of the block as it is usually done in Scala. The `parallelize` macro returns an instance of the class `SystemMLAlgorithm` which includes additional boilerplate code for execution in SystemML. To actually execute the generated code we call:

```Scala
val (w, h) = nmf.run()
```

This will run the generated code on SystemML and return the requested values. Internally, the `SystemMLAlgorithm.run()` method uses SystemML's `MLContext`. The `parallelize` macro can automatically discover that we want `(W, H)` as our return value and makes sure that these are set as outputs in the `MLContext`.

### Input and Output handling

:x: Currently, input and output handling in the Spark REPL and Notebooks is not working without problems. We're working on fixing that!

In the case of NMF we did not load data from any external data-source. In most scenarios, our data will come from some outside source. We provide several ways of passing this data to SystemML. The easiest way is to read data from a file using the builtin `read(...)` primitive that directly maps to SystemML's builtin `read`-primitive.

Additionally, data can be passed from a Spark `DataFrame` that has been created before. The following example shows the usage of passing a dataframe to SystemML:

```Scala
val numRows = 10000
val numCols = 1000
val data = sc.parallelize(0 to numRows-1).map { _ => Row.fromSeq(Seq.fill(numCols)(Random.nextDouble)) }
val schema = StructType((0 to numCols-1).map { i => StructField("C" + i, DoubleType, true) } )
val df = sqlContext.createDataFrame(data, schema)

val alg = parallelize {
      val matrix: Matrix = Matrix.fromDataFrame(df)

      val minOut = min(matrix)
      val maxOut = max(matrix)
      val meanOut = mean(matrix)

      (minOut, maxOut, meanOut)
    }

val  (minOut: Double, maxOut: Double, meanOut: Double) = alg.run()

println(s"The minimum is $minOut, maximum: $maxOut, mean: $meanOut")
```

Similar to the automatic setting of output values, we can automatically find out that the argument to the matrix constructor is a Spark `Dataframe` and set the corresponding input parameter in the `MLContext`.
