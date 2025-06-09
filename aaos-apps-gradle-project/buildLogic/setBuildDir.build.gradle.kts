/*
 * Copyright (C) 2025 The Android Open Source Project
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
import java.io.File
import org.gradle.api.Project

private fun Project.getOutDir(): File {
    // Check the environment first
    project.providers.environmentVariable("OUT_DIR").orNull?.let {
        return File(it)
    }

    // Fallback to project properties if OUT_DIR is not set
    val androidRepoRoot: File by rootProject.gradle.extra
    return androidRepoRoot.resolve("out")
}

// Get the relative path of the current project within the multi-project structure.
private fun Project.getRelativeProjectPath(): String {
    // Traverse up the project hierarchy from the current project to the root project.
    val path =
        generateSequence(this) { it.parent }
            // Stop when the root project is reached.
            .takeWhile { it != rootProject }
            .toList()

    // Put the main projects directly in out/aaos-apps-gradle-build. BuildLogic projects go inside a
    // buildLogic folder
    return if (this == rootProject) {
        this.name
    } else if (rootProject.name == "buildLogic") {
        path.reversed().joinToString(separator = "/") { it.name }.let { "buildLogic/$it" }
    } else {
        path.reversed().joinToString(separator = "/") { it.name }
    }
}

// Send all the build files to the out folder under the root of the repo init.
fun Project.setBuildDir() {
    val outDir = getOutDir()
    val relativePath = getRelativeProjectPath()
    val buildDir = outDir.resolve("aaos-apps-gradle-build").resolve(relativePath)
    project.layout.buildDirectory.set(buildDir)
}

if (project == rootProject) {
    allprojects { setBuildDir() }
}
