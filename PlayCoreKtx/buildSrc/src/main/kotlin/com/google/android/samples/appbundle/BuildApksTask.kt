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

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

/**
 * Invokes the `bundletool build-apks` command with the `--local-testing` flag to produce an APKS
 * file (an APK set) that can be used for local testing of dynamic feature modules or asset packs.
 */
abstract class BuildApksTask @Inject constructor(
        val execOperations: ExecOperations,
        val objectFactory: ObjectFactory
) : DefaultTask() {
    /**
     * The location of the `bundletool` jar
     */
    @get:InputFile
    abstract val bundletoolJar: RegularFileProperty

    /**
     * The location of the input AAB file
     */
    @get:InputFile
    abstract val aabFile: RegularFileProperty

    /**
     * The location for the output APKS file
     */
    @get: OutputFile
    abstract val apksFile: RegularFileProperty

    @TaskAction
    fun run() {
        val aab = aabFile.get().asFile
        val apks = apksFile.get().asFile

        execOperations.javaexec {
            it.classpath = objectFactory.fileCollection().from(bundletoolJar)
            it.args = listOf("build-apks", "--overwrite", "--local-testing", "--bundle", aab.absolutePath, "--output", apks.absolutePath)
        }
    }
}