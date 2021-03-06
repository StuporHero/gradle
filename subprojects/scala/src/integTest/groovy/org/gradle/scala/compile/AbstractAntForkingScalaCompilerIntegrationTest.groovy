/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.scala.compile

abstract class AbstractAntForkingScalaCompilerIntegrationTest extends BasicScalaCompilerIntegrationTest {
    def setup() {
        executer.requireIsolatedDaemons()
        executer.expectDeprecationWarning()
    }

    String compilerConfiguration() {
        '''
tasks.withType(ScalaCompile) {
    scalaCompileOptions.with {
        useAnt = true
        fork = true
        forkOptions.memoryMaximumSize = "512m"
        if(!JavaVersion.current().isJava8Compatible()) {
            forkOptions.jvmArgs = ['-XX:MaxPermSize=512m']
        }
    }
}
'''
    }

    String logStatement() {
        "Compiling with Ant scalac task"
    }

    String getErrorOutput() {
        return result.output
    }
}
