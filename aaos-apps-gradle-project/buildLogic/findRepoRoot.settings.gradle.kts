/*
 * Copyright (C) 2025 The Android Open Source Project
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

/**
 * Try to find the root of the repo/superproject checkout by searching for a .repo or .supermanifest
 * directory, going up the tree from startDir.
 *
 * Also saves a reference to the location of the aaos-apps-gradle-build Gradle project along the
 * way, for use as a fallback if we reach the file system root without finding the repo root.
 */
private fun tryFindRootInFileTree(startDir: File): File? {
    var repoRootDir: File? = null
    var currentDir = startDir

    // The root of the aaos-apps Gradle project
    var gradleRootDir: File? = null

    // Go up the directory tree, try to find the root of the repo
    while (true) {
        if (currentDir.resolve(".repo").exists() || currentDir.resolve(".supermanifest").exists()) {
            repoRootDir = currentDir
            break
        }
        // While we're going up the tree, save the location of the main
        // aaos-apps-gradle-build Gradle project
        if (
            currentDir.resolve("settings.gradle.kts").exists() &&
                currentDir.resolve("buildLogic").exists()
        ) {
            gradleRootDir = currentDir
        }
        // Try to go up one, but break if we've reached the file system root
        currentDir = currentDir.parentFile ?: break
    }

    // Fall back to relative path to the root of the repo/superproject checkout.
    if (repoRootDir == null) {
        println(
            "WARNING: Reached file system root without finding .repo or .supermanifest. Falling back to relative path."
        )
        // The root of the aaos-apps Gradle project should be
        // packages/apps/Car/libs/aaos-apps-gradle-build/, so we go up 5 levels.
        repoRootDir = gradleRootDir?.resolve("../../../../../")
    }
    return repoRootDir
}

// Find the root of the repo/superproject checkout.
// startDir: the directory to start searching in
fun findRepoRoot(startDir: File): File {
    val repoRootDir: File? = tryFindRootInFileTree(startDir)

    if (repoRootDir == null) {
        throw IllegalStateException(
            "Unable to find the root of the repo/superproject checkout. If you see this message, please file a bug report."
        )
    }
    if (!repoRootDir.resolve("out").exists()) {
        println(
            """WARNING:
                Warning! Failed to find `out` directory in at ${repoRootDir.canonicalPath}.
                It could be that this is the first build (it will be created later in
                the build) or that there is some kind of build misconfiguration!
                """
                .trimIndent()
        )
    }
    return repoRootDir
}

// Create an extra property on the "gradle" object that points to the root of the repo/superproject
val androidRepoRoot by gradle.extra(findRepoRoot(rootDir))
