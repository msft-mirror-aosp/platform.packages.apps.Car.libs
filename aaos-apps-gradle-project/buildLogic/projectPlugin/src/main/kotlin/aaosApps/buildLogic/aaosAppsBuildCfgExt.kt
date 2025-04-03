/*
 * Copyright (C) 2023 The Android Open Source Project
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

import java.io.File
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.extraProperties

interface AaosAppsBuildCfgExt {
    val currentSdk: Property<Int>
    val jdkToolchain: Property<Int>
    // Path to the root of the checkout
    val repoRoot: DirectoryProperty
}

fun AaosAppsBuildCfgExt.setDefaults(project: Project) {

    currentSdk.convention(
        project.providers.gradleProperty("aaosApps.buildCfg.currentSdk").map { it.toInt() }
    )

    // Set the default value of the toolchain to the value set in the Gradle properties
    jdkToolchain.convention(
        project.providers.gradleProperty("aaosApps.buildCfg.defaultJdkToolchain").map {
            it.toString().toInt()
        }
    )

    // This property was set in the findRepoRoot.settings.gradle.kts file, which must be included
    // in the build's settings.gradle.kts.
    val androidRepoRoot: File = project.gradle.extraProperties["androidRepoRoot"] as File
    // Set the default value of the repoRoot
    repoRoot.convention(project.layout.dir(project.provider { androidRepoRoot }))
}
