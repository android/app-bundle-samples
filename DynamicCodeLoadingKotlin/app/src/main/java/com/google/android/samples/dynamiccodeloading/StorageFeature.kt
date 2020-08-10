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

import android.content.Context

/**
 * This is the interface that needs
 * to be implemented by our dynamically loaded module.
 *
 * Once we load the module through whichever mechanism we choose,
 * we will always refer to it from the app module using this interface.
 */
interface StorageFeature {
    fun saveCounter(counter: Int)
    fun loadCounter(): Int

    /**
     * StorageFeature can be instantiated in whatever way the implementer chooses,
     * we just want to have a simple method to get() an instance of it.
     */
    interface Provider {
        fun get(dependencies: Dependencies): StorageFeature
    }

    /**
     * Dependencies from the main app module that are required by the StorageFeature.
     */
    interface Dependencies {
        fun getContext(): Context
        fun getLogger(): Logger
    }
}
