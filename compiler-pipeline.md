---
layout: global
title: SystemML Compiler Documentation
description: SystemML Development Environment Setup Guide
---
<!--
{% comment %}
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to you under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
{% endcomment %}
-->

We briefly describe the phases of the SystemML compiler as it is described in [references](#references) [1] and [2]. 
Programs for SystemML are written in a language with R-like syntax called DML (Declarative Machine learning Language). 
Scripts written in DML are then compiled and optimized for execution on hybrid platforms such as local in-memory, 
Hadoop MapReduce, or Apache Spark. [Figure 1] gives an overview over the compilation pipeline.

## Parsing DML Programs

DML scripts are parsed from their textual representation into a hierarchy of statement blocks. Blocks are defined by control flow structures such as if-else-branches, loops or calls to user-defined functions (UDFs)
and contain the respective statements. In the parsing step, lexical and syntactic analysis, as well as resolution of operator precedence is performed. Within the resulting hierarchy of statement blocks, a live variable analysis is 
performed for every block. The final step in this phase is a semantic validation that performs checks for compatibility of (known) dimensions and mandatory parameters of built-in functions. During this phase, also a global constant folding 
and size/dimension propagation is done to replace references to sizes with their concrete values.

To demonstrate the several phases of compilation, we will use a small running example with the following DML code:

    Q = y * (X %*% (b + sb));

After parsing, the AST of this expression is the following:

    Q <- MULT(y, MATRIXMULT(X, PLUS(b, sb)))

Live-variable analysis gives us additional information about the program:

    Live-IN:     y, X, b, sb
    Live-OUT:    Q

In the validation-step, we check if all operands have the required dimensions and assign computed dimensions to Q.
    
    dims(b) == dims(sb), ncol(X) == nrow(bsb), dims(y) == dims(Xbsb)
    dims(Q) <- dims(yXbsb)


## High Level Operator (HOP) Generation

In this phase, the statement blocks that were generated previously are translated into a directed acyclic graph (DAG) of high level operators (HOPs). Nodes in the HOP DAG represent logical operations and their outputs, while edges 
represent data dependencies. HOPs describe operations on a higher level of abstraction such as MatrixMultiply or PersistentWrite. After the initial creation of the DAG from statements and expressions of one block, static rewrites are applied to the graph. 
The rewrites in this phase are assumed to always be beneficial and include algebraic rewrites for simplifications, common subexpression elimination (CSE), constant folding, and branch removal. All rewrites at this stage are size-independent.

Subsequently, an intra-/inter-procedural analysis is performed to propagate size-information inside functions if they are always called with consistent size arguments (inline size information into functions).
After these static rewrites, dynamic, size-specific rewrites are performed on the HOP DAG that are beneficial under specific size conditions. These rewrites include more dynamic algebraic rewrites, e.g. rewrites for matrix multiplication chains. One example for this is diag(XY) -> rowSums(X * Y’), iff y is a vector (ncols(Y) = 1). Another example is the removal of empty operations, e.g. in the case of XY if one of the matrices only consists of zero elements.
The last action in this phase is the computation of worst-case memory estimates for each HOP output. Memory estimates are computed by assuming single node, in-memory control-program computation (CP).

Our example code from above would result in the Following HOP-DAG with annotated size information:

           1
           2
           3
           Put picture here
           5
           6
           7
           8

## Low Level Operator (LOP) Generation

### HOP-LOP Translations

While the HOP DAG specifies the program in an abstract, logical plan with size and memory estimates, the low level operator (LOP) graph includes all information relevant to physical execution. This includes the selection of specific physical operator implementations such as matrix multiplication on Hadoop MapReduce with one operand in distributed cache. In the resulting DAG, nodes represent physical operators and edges represent data dependencies.
One LOP DAG is constructed for each HOP DAG (and therefore for each statement block) and a LOP DAG can contain physical operators for different runtime systems (such as Hadoop MapReduce, Spark, and CP (=single node/control program). An example LOP graph is shown in Figure 1, lower right where we can see the physical operators annotated with their specific runtime systems (MR, CP).

The translation from HOP to LOP is performed in multiple steps per HOP and starts at the root node of a HOP graph. The first step is the decision for MR or CP execution which is based on hard memory constraints and the size estimates for the HOP. If sizes are unknown, a conservative policy is chosen which selects robust MR operators. These HOPs are marked for later recompilation, when better size estimates might be available.

In the second step, each HOP is represented as a subgraph of LOPs that represent the physical operators and their configurations. This requires to decide between multiple operator implementations given the chosen runtime, matrix sizes, and cluster memory characteristics. The HOP graph is maintained together with the LOP graph to allow for later recompilation of HOPs.
The problem of HOP-LOP translation is an optimization problem that depends on cluster characteristics and available metadata. SystemML enables dynamic recompilations of HOPs that are marked as candidates based on delayed availability of size information. if this information becomes available during computation of previous instructions, the HOPs can be recompiled using the new information.

### Piggybacking

In a last optimization step the final runtime program is generated. This runtime program consists of a list of instructions for the respective target-system (CP, MR, Spark). To reduce latency and increase potential data-sharing, multiple instructions for distributed systems (MR, Spark) are packed into single jobs/instructions. This process is called piggybacking reduces the total amount of necessary distributed jobs and therefore minimizes the overhead that comes with instructions for distributed runtimes.  
    
<br><br>
  
#### References<a name="references"></a>

[1] Ghoting, Amol; Krishnamurthy, R.; Pednault, E.; Reinwald, B.; Sindhwani, V.; Tatikonda, S.; Yuanyuan Tian; Vaithyanathan, S., "SystemML: Declarative machine learning on MapReduce," in Data Engineering (ICDE), 2011 IEEE 27th International Conference on , vol., no., pp.231-242, 11-16 April 2011

[2] Matthias Boehm; Douglas R. Burdick; Alexandre V. Evfimievski; Berthold Reinwald; Frederick R. Reiss; Prithviraj Sen; Shirish Tatikonda; Yuanyuan Tian, “SystemML's Optimizer: Plan Generation for Large-Scale Machine Learning Programs,” in IEEE Data Eng. Bull. 37(3), 2014
