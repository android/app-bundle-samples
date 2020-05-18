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
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import com.google.android.samples.playcore.picture.R
import com.google.android.samples.playcore.picture.databinding.PictureBinding

/**
 * This fragment enables taking a picture.
 */
class PictureFragment : Fragment(R.layout.picture) {

    private lateinit var viewBinding: PictureBinding

    private lateinit var getPicturePreview: ActivityResultLauncher<Void>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPicturePreview = requireActivity().registerForActivityResult(
            TakePicturePreview(),
            ::generateSwatches
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        PictureBinding.inflate(layoutInflater)
            .apply {
                viewBinding = this
                openCamera.setOnClickListener {
                    getPicturePreview.launch(null)
                }
            }.root

    private fun generateSwatches(bitmap: Bitmap) {
        applySwatches(Palette.from(bitmap).generate().swatches)
    }

    private fun applySwatches(swatches: List<Swatch>) {
        Log.d("PictureFragment", "swatches: $swatches")

        // TODO Update UI with colored buttons depending on image's vibrant and muted colors
    }
}
