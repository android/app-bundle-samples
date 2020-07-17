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

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.dsl.ApplicationExtension
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
        project.extensions.getByType(ApplicationExtension::class.java).onVariantProperties {
            val buildApksTask = project.tasks.register(
                    "${name}BuildApksForLocalTesting",
                    BuildApksTask::class.java
            ) {
                it.aabFile.set(artifacts.get(ArtifactType.BUNDLE))
                it.apksFile.set(it.aabFile.map { aab ->
                    project.layout.buildDirectory
                            .file("outputs/apks/$name/${aab.asFile.name}.apks")
                            .get()
                })
                it.bundletoolJar.set(project.rootProject.file(BUNDLETOOL_PATH))
            }

            project.tasks.register(
                    "${name}InstallApksForLocalTesting",
                    InstallApksTask::class.java
            ) {
                it.apksFile.set(buildApksTask.flatMap(BuildApksTask::apksFile))
                it.bundletoolJar.set(project.rootProject.file(BUNDLETOOL_PATH))
            }
        }
    }
}

const val BUNDLETOOL_PATH = "third_party/bundletool/bundletool-all-1.0.0.jar"