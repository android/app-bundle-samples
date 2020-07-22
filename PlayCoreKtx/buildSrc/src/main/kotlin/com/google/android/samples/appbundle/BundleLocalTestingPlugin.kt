/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("UnstableApiUsage")

package com.google.android.samples.appbundle

import com.android.SdkConstants
import com.android.build.api.artifact.ArtifactType
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The plugin makes 2 new tasks available per app variant:
 * `<variant-name>BuildApksForLocalTesting` and `<variant-name>InstallApksForLocalTesting`.
 *
 * They enable building and installing an APKS (APK set) in local testing mode from the Android
 * application bundle.
 */
abstract class BundleLocalTestingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withType(AppPlugin::class.java) {
            val android = project.extensions.getByName("android") as BaseAppModuleExtension
            android.onVariants {
                // we currently don't support passing signing configs to the build apks task
                // so let's only create the tasks for debug variants or return early otherwise
                if (!debuggable) {
                    return@onVariants
                }
                onProperties {
                    val buildApksTask = project.tasks.register(
                            "${name}BuildApksForLocalTesting",
                            BuildApksTask::class.java
                    ) { buildApksTask ->
                        buildApksTask.aabFile.set(artifacts.get(ArtifactType.BUNDLE))
                        buildApksTask.apksFile.set(buildApksTask.aabFile.flatMap { aab ->
                            project.layout.buildDirectory
                                    .file("outputs/apks-for-local-testing/$name/${aab.asFile.name}.apks")
                        })
                        buildApksTask.aapt2Executable.set(android.sdkComponents.sdkDirectory.map {
                            it.file("build-tools/${android.buildToolsVersion}/${SdkConstants.FN_AAPT2}")
                        })
                    }

                    project.tasks.register(
                            "${name}InstallApksForLocalTesting",
                            InstallApksTask::class.java
                    ) {
                        it.apksFile.set(buildApksTask.flatMap(BuildApksTask::apksFile))
                        it.adbExecutable.set(android.sdkComponents.adb)
                    }
                }
            }
        }
    }
}
