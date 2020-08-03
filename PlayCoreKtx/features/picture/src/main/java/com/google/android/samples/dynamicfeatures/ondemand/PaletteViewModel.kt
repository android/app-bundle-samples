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

package com.google.android.samples.dynamicfeatures.ondemand

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaletteViewModel : ViewModel() {

    private val _swatchesLiveData = MutableLiveData<List<Swatch>>()
    val swatches: LiveData<List<Swatch>> = _swatchesLiveData

    fun requestPalette(bitmap: Bitmap) {
        viewModelScope.launch {
            val palette = withContext(Dispatchers.Default) {
                Palette.from(bitmap).generate()
            }
            palette.swatches.let {
                _swatchesLiveData.value = it
            }
        }
    }
}
