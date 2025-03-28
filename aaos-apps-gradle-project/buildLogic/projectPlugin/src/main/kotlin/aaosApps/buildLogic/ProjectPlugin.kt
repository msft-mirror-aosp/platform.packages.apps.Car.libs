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
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.CompileClasspathNormalizer
import org.gradle.api.tasks.compile.JavaCompile

private fun Project.configureJavaCompile(extension: AaosAppsBuildCfgExt) {
    val systemStubsSdk = project.properties["aaosApps.buildCfg.systemStubsSdk"]

    val systemStubs = extension.repoRoot.file("prebuilts/sdk/$systemStubsSdk/system/android.jar")

    tasks.withType(JavaCompile::class.java).configureEach { task ->
        val objectFactory = project.objects

        task.inputs.files(systemStubs).withNormalizer(CompileClasspathNormalizer::class.java)
        task.doFirst {
            task.classpath = objectFactory.fileCollection().from(systemStubs, task.classpath)
        }

        // Enable some lints
        // TODO: just pass -Xlint, to enable all the supported types of warnings.
        task.options.compilerArgs.add("-Xlint:unchecked")
        task.options.compilerArgs.add("-Xlint:deprecation")
    }
}

class ProjectPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Set up the extension for the plugin
        val extension =
            project.extensions.create("aaosAppsBuildCfg", AaosAppsBuildCfgExt::class.java)
        extension.setDefaults(project)

        // Set the JDK to use for the project
        project.setJDK(extension)

        // This block will trigger after an Android plugin is loaded
        project.plugins.withType(AndroidBasePlugin::class.java) {

            // Add in the System stubs and other Java compile configuration
            project.configureJavaCompile(extension)

            project.extensions.getByType(CommonExtension::class.java).apply {
                val ace = project.extensions.getByType(AndroidComponentsExtension::class.java)

                compileSdk =
                    project.findProperty("aaosApps.buildCfg.compileSdk")!!.toString().toInt()
                buildToolsVersion =
                    project.findProperty("aaosApps.buildCfg.buildToolsVersion")!!.toString()

                // NDK: Check if we're using the prebuilt SDK, and if so set the path to the
                // prebuilt NDK
                val sdkDir = ace.sdkComponents.sdkDirectory.get().asFile
                if (sdkDir.path.contains("prebuilts/fullsdk")) {
                    ndkPath = sdkDir.resolve("ndk-bundle").absolutePath
                }

                // Load the NDK version from the Gradle properties.
                // When building with the prebuilt NDK, this must match the prebuilt NDK's version
                ndkVersion = project.findProperty("aaosApps.buildCfg.ndkVersion")!!.toString()

                // The default location for the native build directory is relative to the module,
                // which mucks up the git staging. We need to move it out of the module, but it
                // can't be within the build directory, so the below line creates a
                // cmake-build-staging directory in the same directory that holds the rest of the
                // build directories (out/aaos-apps-gradle-build)
                externalNativeBuild.cmake {
                    // Load the CMake version from the Gradle properties.
                    version = project.findProperty("aaosApps.buildCfg.cmakeVersion")!!.toString()
                    buildStagingDirectory =
                        project.rootProject.layout.buildDirectory
                            .dir("../cmake-build-staging/${project.name}")
                            .get()
                            .asFile
                }
            }
        }
    }
}
