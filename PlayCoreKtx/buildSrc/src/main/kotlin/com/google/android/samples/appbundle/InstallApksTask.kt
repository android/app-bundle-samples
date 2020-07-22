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

import com.android.tools.build.bundletool.commands.InstallApksCommand
import com.android.tools.build.bundletool.device.DdmlibAdbServer
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Invokes the `bundletool install-apks` command to install splits from an APKS file on a connected
 * device.
 */
abstract class InstallApksTask : DefaultTask() {
    /**
     * The location of the input APKS file
     */
    @get:InputFile
    abstract val apksFile: RegularFileProperty

    /**
     * The location of the adb executable file
     */
    @get:Internal
    abstract val adbExecutable: RegularFileProperty

    @TaskAction
    fun run() {
        InstallApksCommand.builder()
                .setApksArchivePath(apksFile.get().asFile.toPath())
                .setAdbServer(DdmlibAdbServer.getInstance())
                .setAdbPath(adbExecutable.get().asFile.toPath())
                .build().execute()
    }
}
