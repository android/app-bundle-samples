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
package com.google.android.samples.storage.di

import android.content.Context
import android.preference.PreferenceManager
import com.google.android.samples.dynamiccodeloading.StorageFeature
import com.google.android.samples.storage.StorageFeatureImpl
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Singleton
@Component(
    modules = [StorageModule::class],
    dependencies = [StorageFeature.Dependencies::class] // needs dependencies passed in to create component
)
interface StorageComponent {
    fun storageFeature(): StorageFeature
}

/**
 * You can have your own modules, providers etc. in this component to build your object graph.
 * You have access to objects provided by the StorageFeature.Dependencies interface from the base component.
 */
@Module
class StorageModule {
    @Provides
    internal fun provideSharedPreferences(ctx: Context) = PreferenceManager.getDefaultSharedPreferences(ctx)

    @Provides
    internal fun bindStorageFeatureImpl(storageModule: StorageFeatureImpl): StorageFeature = storageModule
}
