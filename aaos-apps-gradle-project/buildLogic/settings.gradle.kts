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

@file:Suppress("UnstableApiUsage")

/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 * For more detailed information on multi-project builds, please refer to https://docs.gradle.org/8.10/userguide/multi_project_builds.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

rootProject.name = "buildLogic"

include("settingsPlugin")
include("projectPlugin")

// Send all the build files to the out folder under the root of the repo init.
gradle.lifecycle.beforeProject {

    // Find the root of the repo/superproject checkout
    fun findRepoRoot(project: Project): File {
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
        // relative to the project's rootDir, and the `buildLogic` folder is in
        // packages/apps/Car/libs/aaos-apps-gradle-build/buildLogic, so we go up 5 levels.
        val rootDir = project.rootDir.resolve("../../../../../../")
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
    // Set the build directory relative to the OUT_DIR. If OUT_DIR isn't set then locate it
    // relative to the repo/superproject's root.
    val outDirFromEnv = project.providers.environmentVariable("OUT_DIR").orNull?.let { File(it) }
    val outDir = outDirFromEnv ?: findRepoRoot(project).resolve("out")
    layout.buildDirectory.set(outDir.resolve("aaos-apps-gradle-build/buildLogic/$name"))
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
            // Let's use the latest Kotlin version here
            version("kotlin", "2.0.20")
        }
    }
}
