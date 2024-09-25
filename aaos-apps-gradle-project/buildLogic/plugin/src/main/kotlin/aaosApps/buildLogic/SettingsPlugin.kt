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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import java.io.File

@Suppress("UnstableApiUsage")
class SettingsPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        setBuildDir(target)
    }

    // Send all the build files to the out folder under the root of the repo init.
    private fun setBuildDir(target: Settings) {
        target.gradle.lifecycle.beforeProject { project ->
            val outDirFromEnv =
                project.providers.environmentVariable("OUT_DIR").orNull?.let { File(it) }
            val outDir = outDirFromEnv ?: findRepoRoot(project).resolve("out")

            // Assemble list of the project's parents, not including the rootProject.
            var projPath = ""
            val parents: Sequence<Project> =
                generateSequence(project) {
                    it.parent?.takeIf { parentProj -> parentProj.name != project.rootProject.name }
                }
            for (current in parents) {
                projPath = "${current.name}/$projPath"
            }

            val outGradleBuildDir = outDir.resolve("aaos-apps-gradle-build")
            project.layout.buildDirectory.set(outGradleBuildDir.resolve(projPath))
        }
    }

    // Find the root of the repo/superproject checkout
    private fun findRepoRoot(project: Project): File {
        var currentDir = project.rootDir
        while (true) {
            if (File(currentDir, ".repo").exists() || File(currentDir, ".supermanifest").exists()) {
                return currentDir
            }
            currentDir = currentDir.parentFile ?: break
        }
        project.logger.warn(
            "Reached file system root without finding .repo or .supermanifest. Falling back to relative path."
        )
        // Fall back to relative path to the root of the repo/superproject checkout. This is
        // relative to the project's rootDir, and the `aaos-apps-gradle-build` folder is in
        // packages/apps/Car/libs/aaos-apps-gradle-build, so we go up 5 levels.
        val rootDir = project.rootDir.resolve("../../../../../")
        if (!rootDir.resolve("out").exists()) {
            project.logger.warn(
                """
                Warning! Failed to find `out` directory in at ${rootDir.canonicalPath}.
                It could be that this is the first build (it will be created later in
                the build) or that there is some kind of build misconfiguration!
                """
                    .trimIndent()
            )
        }
        return rootDir
    }
}
