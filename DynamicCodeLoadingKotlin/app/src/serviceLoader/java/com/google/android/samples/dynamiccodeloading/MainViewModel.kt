/*
 * Copyright 2019 Google LLC
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
 *
 */
package com.google.android.samples.dynamiccodeloading

import android.app.Application
import android.content.Context
import android.util.Log
import java.util.ServiceLoader

/**
 * An implementation of our ViewModel that uses ServiceLoader.
 *
 * ServiceLoader is a standard mechanism in Java that is used to load
 * concrete service implementations (as defined by an interface)
 * based on metadata found in META-INF/services/
 *
 * You can find the corresponding metadata file in the storage module
 * in storage/src/serviceLoader/resources/META-INF/services
 *
 * Please note that by default, ServiceLoader involves disk access
 * and performs slow operations, making the performance of this solution
 * not optimal for use in Android apps.
 *
 * R8 (the new code shrinker and optimizer in Android Studio 3.4)
 * introduced an optimization where for straightforward ServiceLoader uses
 * it is able to replace them with straight object instantiation based
 * on the same metadata from META-INF at build time,
 * mitigating the performance and disk access issues.
 *
 * At the time of writing this sample, a specific version of R8 from master
 * has to be used. The optimization should be in versions of R8 included with
 * Android Gradle Plugin 3.5.0+.
 *
 * The ServiceLoader approach can be useful if you want to load
 * multiple implementations of a service. Even though in this sample we only
 * have one StorageFeature implementation, ServiceLoader.load returns an iterator
 * with all registered classes.
 */
class MainViewModel(app: Application) : AbstractMainViewModel(app) {
    override fun initializeStorageFeature() {
        // We will need this to pass in dependencies to the StorageFeature.Provider
        val dependencies: StorageFeature.Dependencies = object : StorageFeature.Dependencies {
            override fun getContext(): Context = getApplication()
            override fun getLogger(): Logger = MainLogger
        }

        // Ask ServiceLoader for concrete implementations of StorageFeature.Provider
        // Explicitly use the 2-argument version of load to enable R8 optimization.
        val serviceLoader = ServiceLoader.load(
            StorageFeature.Provider::class.java,
            StorageFeature.Provider::class.java.classLoader
        )

        // Explicitly ONLY use the .iterator() method on the returned ServiceLoader to enable R8 optimization.
        // When these two conditions are met, R8 replaces ServiceLoader calls with direct object instantiation.
        storageModule = serviceLoader.iterator().next().get(dependencies)
        Log.d(TAG, "Loaded storage feature through ServiceLoader")
    }
}
