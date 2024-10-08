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

package aaosApps.buildLogic.project

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

class ProjectPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val versionCatalog =
            target.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

        target.plugins.withType(AndroidBasePlugin::class.java) {
            target.extensions.getByType(CommonExtension::class.java).apply {
                compileSdk = versionCatalog.findVersion("compileSdk").get().toString().toInt()
                buildToolsVersion = versionCatalog.findVersion("buildToolsVersion").get().toString()
            }
        }
    }
}
