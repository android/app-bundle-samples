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
    id("com.android.dynamic-feature")
    kotlin("android")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(project(":app"))

    // Testing
    debugImplementation("androidx.fragment:fragment-testing:1.2.5")

    // Testing - JVM
    testImplementation("androidx.test.ext:junit:1.1.1")
    testImplementation("androidx.test.ext:truth:1.2.0")
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("androidx.test.espresso:espresso-core:3.2.0")
    testImplementation("androidx.test.espresso:espresso-contrib:3.2.0")
    testImplementation("androidx.test.espresso:espresso-intents:3.2.0")
    testImplementation("androidx.navigation:navigation-testing:2.3.0")
}
