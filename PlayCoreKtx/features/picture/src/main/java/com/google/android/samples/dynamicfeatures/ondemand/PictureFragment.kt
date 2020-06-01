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

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels

/**
 * This fragment enables taking a picture and feeding it into the [PaletteViewModel].
 */
class PictureFragment : Fragment() {

    // Using activityViewModels here as Palette is passed into another fragment once it's generated.
    private val paletteViewModel by activityViewModels<PaletteViewModel>()

    private lateinit var getPicturePreview: ActivityResultLauncher<Void>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPicturePreview = requireActivity().registerForActivityResult(TakePicturePreview()) { it ->
            if (it == null) {
                requireActivity().finish()
            } else {
                paletteViewModel.requestPalette(it)
            }
        }
        getPicturePreview.launch(null)
    }
}
