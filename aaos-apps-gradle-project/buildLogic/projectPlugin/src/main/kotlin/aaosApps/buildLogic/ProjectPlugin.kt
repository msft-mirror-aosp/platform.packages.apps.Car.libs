/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aaosApps.buildLogic

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class ProjectPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension =
            project.extensions.create("aaosAppsBuildCfg", aaosAppsBuildCfgExt::class.java)
        // Set the default value of the toolchain to the value set in the Gradle properties
        val mappedToolChainVersionProvider =
            project.providers.gradleProperty("aaosApps.buildCfg.defaultJdkToolchain").map {
                it.toString().toInt()
            }
        extension.jdkToolchain.convention(mappedToolChainVersionProvider)

        project.setJDK(extension)

        project.plugins.withType(AndroidBasePlugin::class.java) {
            project.extensions.getByType(CommonExtension::class.java).apply {
                compileSdk =
                    project.findProperty("aaosApps.buildCfg.compileSdk")!!.toString().toInt()
                buildToolsVersion =
                    project.findProperty("aaosApps.buildCfg.buildToolsVersion")!!.toString()
            }
        }
    }
}
