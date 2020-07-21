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
import android.util.Log
import android.widget.FrameLayout.LayoutParams
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.observe
import com.google.android.samples.dynamicfeatures.ui.BaseSplitActivity
import com.google.android.samples.playcore.picture.R

/** Activity to take pictures and get palettes. */
class PictureActivity : BaseSplitActivity() {

    private val viewModel by viewModels<PaletteViewModel>()
    private lateinit var getPicturePreview: ActivityResultLauncher<Void>
    private var resultHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FragmentContainerView(this).apply {
            id = R.id.fragmentContainer
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        })
        getPicturePreview = registerForActivityResult(TakePicturePreview()) {
            if (it == null) {
                finish()
            } else {
                viewModel.requestPalette(it)
            }
            resultHandled = true
        }

        viewModel.swatches.observe(this) {
            if (it.isNotEmpty()) {
                displayPaletteFragment()
            } else {
                Log.w(TAG, "Issue generating Palette, attempting to get new picture.")
                getPicturePreview.launch(null)
            }
        }
    }

    override fun onResume() {
        if (!resultHandled) getPicturePreview.launch(null)
        super.onResume()
    }

    private fun displayPaletteFragment() = replaceFragment(PaletteFragment())

    private fun replaceFragment(fragment: Fragment) {
        with(supportFragmentManager) {
            if (findFragmentById(R.id.fragment) == null) {
                beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit()
            }
        }
    }

    companion object {
        private const val TAG = "PictureActivity"
    }
}
