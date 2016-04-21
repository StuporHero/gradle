/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.plugin.use

import groovy.transform.NotYetImplemented
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.util.GradleVersion

import static org.hamcrest.Matchers.containsString

class PluginRepositoriesDslSpec extends AbstractIntegrationSpec {

    def "pluginRepositories block can be read from settings.gradle"() {
        given:
        settingsFile << """
            pluginRepositories {}
        """

        expect:
        succeeds 'help'
    }

    def "pluginRepositories block supports defining a maven plugin repository"() {
        given:
        settingsFile << """
            pluginRepositories {
                maven {
                    name "ourMavenRepo"
                    url "http://repo.internal.net/m2"
                }
            }
        """

        expect:
        succeeds 'help'
    }


    def "pluginRepositories block is not supported in ProjectScripts"() {
        given:
        buildScript """
            pluginRepositories {}
        """

        when:
        fails 'help'

        then:
        failure.assertHasLineNumber(2)
        failure.assertThatCause(containsString("Only Settings and Init scripts can contain a pluginRepositories {} block."))
        includesLinkToUserguide()
    }

    def "pluginRepositories block must come before other blocks in the settings.gradle script"() {
        given:
        settingsFile << """
            rootProject.name = 'rumpelstiltskin'
            pluginRepositories {}
        """

        when:
        fails 'help'

        then:
        failure.assertHasLineNumber(3)
        failure.assertThatCause(containsString("The pluginRepositories {} block must appear before any other statements in the script."))
        includesLinkToUserguide()
    }

    @NotYetImplemented
    def "pluginRepositories block must be a top-level block (not nested)"() {
        given:
        settingsFile << """
            if (true) {
                pluginRepositories {}
            }
        """

        when:
        fails 'help'

        then:
        failure.assertHasLineNumber(3)
        failure.assertThatCause(containsString("The pluginRepositories {} block must appear before any other statements in the script."))
        includesLinkToUserguide()
    }

    def "Only one pluginRepositores block is allowed in each script"() {
        given:
        settingsFile << """
            pluginRepositories {}
            pluginRepositories {}
        """

        when:
        fails 'help'

        then:
        failure.assertHasLineNumber(3)
        failure.assertThatCause(containsString("At most, one pluginRepositories {} block may appear in the script."))
        includesLinkToUserguide()
    }

    void includesLinkToUserguide() {
        failure.assertThatCause(containsString("https://docs.gradle.org/${GradleVersion.current().getVersion()}/userguide/plugins.html#sec:plugin_repositories"))
    }
}
