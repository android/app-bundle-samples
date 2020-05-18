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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.samples.playcore.randomcolor.R
import com.google.android.samples.playcore.randomcolor.databinding.RandomColorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fragment that displays a random color.
 * Color changes automatically after a defined delay or via click on the screen.
 */
class RandomColorFragment : Fragment(R.layout.random_color) {

    private val colorGenerator = ColorGenerator()
    private lateinit var viewBinding: RandomColorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return RandomColorBinding.inflate(layoutInflater)
            .apply {
                viewBinding = this
            }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            content.setOnClickListener {
                randomContentBackground()
            }

            lifecycleScope.launch {
                changeBackgroundColor(CHANGE_INTERVAL)
            }
        }
    }

    /**
     * Changes the background color after a given [intervalMs].
     */
    private suspend fun changeBackgroundColor(intervalMs: Long) {
        while (true) {
            delay(intervalMs)
            randomContentBackground()
        }
    }

    private fun randomContentBackground() {
        with(viewBinding) {
            val randomColor = colorGenerator.randomColor
            content.setBackgroundColor(randomColor)
        }
    }

    companion object {
        const val CHANGE_INTERVAL = 2000L
    }
}
