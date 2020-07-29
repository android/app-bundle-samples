import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput

/*
 * Copyright 2020 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.google.android.samples.dynamicnavigator"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 3
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dynamicFeatures = mutableSetOf(":dynamicfeature", ":includedgraphfeature")

    packagingOptions {
        exclude("META-INF/**.version")
    }
}

dependencies {
    api("androidx.navigation:navigation-dynamic-features-fragment:2.3.0")
    api("androidx.appcompat:appcompat:1.1.0")
    api("androidx.constraintlayout:constraintlayout:1.1.3")
}

val bundletoolJar = project.rootDir.resolve("third_party/bundletool/bundletool-all-0.13.0.jar")

android.applicationVariants.all(object : Action<ApplicationVariant> {
    override fun execute(variant: ApplicationVariant) {
        variant.outputs.forEach { output: BaseVariantOutput? ->
            (output as? ApkVariantOutput)?.let { apkOutput: ApkVariantOutput ->
                var filePath = apkOutput.outputFile.absolutePath
                filePath = filePath.replaceAfterLast(".", "aab")
                filePath = filePath.replace("build/outputs/apk/", "build/outputs/bundle/")
                var outputPath = filePath.replace("build/outputs/bundle/", "build/outputs/apks/")
                outputPath = outputPath.replaceAfterLast(".", "apks")

                tasks.register<JavaExec>("buildApks${variant.name.capitalize()}") {
                    classpath = files(bundletoolJar)
                    args = listOf(
                        "build-apks",
                        "--overwrite",
                        "--local-testing",
                        "--bundle",
                        filePath,
                        "--output",
                        outputPath
                    )
                    dependsOn("bundle${variant.name.capitalize()}")
                }

                tasks.register<JavaExec>("installApkSplitsForTest${variant.name.capitalize()}") {
                    classpath = files(bundletoolJar)
                    args = listOf("install-apks", "--apks", outputPath)
                    dependsOn("buildApks${variant.name.capitalize()}")
                }
            }
        }
    }
})
