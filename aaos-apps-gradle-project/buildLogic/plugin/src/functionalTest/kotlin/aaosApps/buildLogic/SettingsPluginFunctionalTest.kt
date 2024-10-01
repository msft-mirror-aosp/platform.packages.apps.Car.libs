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

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir

class SettingsPluginFunctionalTest {

  @field:TempDir lateinit var projectDir: File

  private val buildFile by lazy { projectDir.resolve("build.gradle.kts") }
  private val settingsFile by lazy { projectDir.resolve("settings.gradle.kts") }
  // Gotta give the test a root dir to find so that the plugin doesn't fall back to using
  // "../../../../.."
  private val supermanFile by lazy { projectDir.resolve(".supermanifest") }

  @Test
  fun buildDirSet() {
    // Set up the test build
    settingsFile.writeText(
        """
            plugins {
                id("aaosApps.buildLogic.settings")
            }
        """
            .trimIndent())
    buildFile.writeText(
        """
            println("My buildDir is " + project.layout.buildDirectory.get().toString())
        """
            .trimIndent())
    supermanFile.writeText("PLACEHOLDER")

    // Run the build
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withProjectDir(projectDir)
    val result = runner.build()

    // Verify the result
    assertTrue(result.output.contains(Regex("My buildDir is .*out/aaos-apps-gradle-build")))
  }
}
