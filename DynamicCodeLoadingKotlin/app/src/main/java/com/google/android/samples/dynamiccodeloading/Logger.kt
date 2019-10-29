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

import android.util.Log

/**
 * Just a simple logger.
 *
 * This has been added to the sample just to show how to deal
 * with dynamically loaded classes from on-demand features that
 * require multiple dependencies.
 */
interface Logger {
    fun log(message: String)
}

const val TAG = "DynamicCodeLoading"

object MainLogger : Logger {
    override fun log(message: String) {
        Log.d(TAG, message)
    }
}
