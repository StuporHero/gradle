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

package org.gradle.initialization

import org.gradle.api.internal.plugins.dsl.PluginRepositoryHandler
import org.gradle.api.internal.plugins.repositories.ScriptScopedPluginRepositoryHandler
import org.gradle.internal.reflect.Instantiator
import org.gradle.util.ConfigureUtil

/**
 * Endows an {@link InitScript} with methods which can only be used when
 * processing top-level blocks on the initial pass through a script.
 */
abstract class InitialPassInitScript extends InitScript {
    public PluginRepositoryHandler getPluginRepositoryHandler() {
        Instantiator instantiator = __scriptServices.get(Instantiator.class);
        PluginRepositoryHandler pluginRepositoryHandler = __scriptServices.get(PluginRepositoryHandler.class);
        return instantiator.newInstance(
            ScriptScopedPluginRepositoryHandler.class, pluginRepositoryHandler, getFileResolver(), instantiator);
    }

    public void pluginRepositories(Closure config) {
        ConfigureUtil.configure(config, getPluginRepositoryHandler())
    }
}
