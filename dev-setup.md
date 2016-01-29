---
layout: global
title: SystemML Developer Setup
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

## Overview

This guide will show you how to set up a fresh SystemML project in Intellij and configure a debug configuration.
    
To run and debug DML scripts from inside Intellij we can setup run/debug configurations that essentially do the same as
the `/bin/systemml` script. This lets us set breakpoints in the code to observe and debug the compilation 
and execution process of a provided DML script.

## Setup

**Get SystemML source from Github:**

    git clone git@github.com:apache/incubator-systemml.git
    
**Import project into Intellij:**

    File -> New -> Project from existing sources...
    
open the `incubator-systemml` folder and import it as a maven project.
    
**Build project and setup runtime dependencies:**
    
To run the scripts from inside Intellij we have to add some runtime-dependencies to our project structure. 
To do this, first build the project:

    cd incubator-systemml/
    mvn clean package
    
Add the required dependencies:

    File -> Project Structure... -> Modules
    
![add-dependencies](img/devdocs/setup/add-dependencies.png "Add dependencies")

Click "JARs or directories..." and select `target/classes` and `target/lib` to add them. Change the 
scope for both dependencies to `runtime` and click `Apply`.

**Run/Debug Configuration:**

Now we can configure our debug configuration:

![debug-conf](img/devdocs/setup/debug-config.png "Debug Configuration Setup")

As Main class we use `org.apache.sysml.api.DMLScript` and set the VM-options according to our local setup, e.g.

VM options:  
    
`-Xmx8g -Xms4g -Xmn1g -Dlog4j.configuration=file:/home/incubator-systemml/conf/log4j.properties -Duser.dir=/home/incubator-systemml`
    
Program arguments: 

`-f /path/to/myscript.dml -exec singlenode -config=/home/incubator-systemml/conf/SystemML-config.xml -nvargs myarg1 myarg2`
    

    

    

