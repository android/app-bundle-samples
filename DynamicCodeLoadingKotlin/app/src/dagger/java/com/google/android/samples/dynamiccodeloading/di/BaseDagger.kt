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
package com.google.android.samples.dynamiccodeloading.di

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.samples.dynamiccodeloading.Logger
import com.google.android.samples.dynamiccodeloading.StorageFeature
import com.google.android.samples.dynamiccodeloading.TAG
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

const val PROVIDER_CLASS = "com.google.android.samples.storage.StorageFeatureImpl\$Provider"

@Component(modules = [BaseModule::class])
interface BaseComponent : StorageFeature.Dependencies {

    fun storageFeature(): StorageFeature?

    @Component.Builder
    interface Builder {
        @BindsInstance fun application(application: Application): Builder
        @BindsInstance fun logger(logger: Logger): Builder
        fun build(): BaseComponent
    }
}

@Module
object BaseModule {
    private var storageFeature: StorageFeature? = null

    /**
     * This method will return null until the required on-demand feature is installed.
     * It will cache the value the first time a non-null value is returned.
     */
    @Provides
    @JvmStatic
    fun storageFeatureProvider(baseComponent: BaseComponent): StorageFeature? {
        if (storageFeature != null) {
            return storageFeature as StorageFeature
        }
        try {
            // Get the instance of the StorageFeature.Provider, pass it the BaseComponent which fulfills the
            // StorageFeature.Dependencies contract, and get the StorageFeature instance in return.
            val provider = Class.forName(PROVIDER_CLASS).kotlin.objectInstance as StorageFeature.Provider
            return provider.get(baseComponent)
                .also { storageFeature = it } // cache the value for later calls
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "Provider class not found", e)
            return null
        }
    }

    @Provides
    @JvmStatic
    fun appContextProvider(application: Application): Context = application
}
