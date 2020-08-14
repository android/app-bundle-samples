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
package com.google.android.samples.dynamicfeatures.state

import android.graphics.Color
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
class ColorViewModel : ViewModel() {
    /** Controls whether the flashlight is on */
    val lightsOn = MutableStateFlow<Boolean>(false)

    val backgroundColor = MutableStateFlow(Color.YELLOW)

    /** Indicates whether the color was last set as a result of using the photo color picker. */
    var colorWasPicked: Boolean = false
        private set

    init {
        backgroundColor
                .drop(1) // ignore first value that is set above
                .onEach {
                    colorWasPicked = true
                }.launchIn(viewModelScope)
    }

    fun notifyPickedColorConsumed() {
        colorWasPicked = false
    }
}