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

import com.android.tools.build.bundletool.commands.BuildApksCommand
import com.android.tools.build.bundletool.commands.DebugKeystoreUtils
import com.android.tools.build.bundletool.model.Aapt2Command
import com.android.tools.build.bundletool.model.utils.SystemEnvironmentProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Invokes the `bundletool build-apks` command with the `--local-testing` flag to produce an APKS
 * file (an APK set) that can be used for local testing of dynamic feature modules or asset packs.
 *
 * It doesn't currently support custom signing configs and always tries to use the default debug
 * android key store.
 */
abstract class BuildApksTask : DefaultTask() {
    /**
     * The location of the `aapt2` executable
     */
    @get:InputFile
    abstract val aapt2Executable: RegularFileProperty

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

        val debugConfig = DebugKeystoreUtils.getDebugSigningConfiguration(SystemEnvironmentProvider.DEFAULT_PROVIDER)

        BuildApksCommand.builder()
                .setAapt2Command(
                        Aapt2Command.createFromExecutablePath(aapt2Executable.get().asFile.toPath())
                )
                .setOverwriteOutput(true)
                .setLocalTestingMode(true)
                .setBundlePath(aab.toPath())
                .setOutputFile(apks.toPath())
                .setSigningConfiguration(debugConfig.get())
                .build().execute()
    }
}
