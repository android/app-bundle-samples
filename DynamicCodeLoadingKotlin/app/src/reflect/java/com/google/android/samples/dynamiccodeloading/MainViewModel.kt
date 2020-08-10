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

const val PROVIDER_CLASS = "com.google.android.samples.storage.StorageFeatureImpl\$Provider"

class MainViewModel(app: Application) : AbstractMainViewModel(app) {
    override fun initializeStorageFeature() {
        // We will need this to pass in dependencies to the StorageFeature.Provider
        val dependencies = object : StorageFeature.Dependencies {
            override fun getContext(): Context = getApplication()
            override fun getLogger(): Logger = MainLogger
        }
        // Our provider is implemented as a Kotlin singleton object, that's why we access it with ".objectInstance"
        val storageModuleProvider = Class.forName(PROVIDER_CLASS).kotlin.objectInstance as StorageFeature.Provider

        // Get an implementation of StorageFeature using the StorageFeature.Provider interface
        storageModule = storageModuleProvider.get(dependencies)
        Log.d(TAG, "Loaded storage feature through reflection")
    }
}
