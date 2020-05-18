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
import android.widget.FrameLayout.LayoutParams
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.fragment.app.FragmentContainerView
import com.google.android.samples.dynamicfeatures.BaseSplitActivity
import com.google.android.samples.playcore.randomcolor.R

/** Activity to hold [RandomColorFragment]. */
class RandomColorActivity : BaseSplitActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FragmentContainerView(this).apply {
            id = R.id.fragmentContainer
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        })

        with(supportFragmentManager) {
            if (findFragmentById(R.id.fragment) == null) {
                beginTransaction()
                    .add(R.id.fragmentContainer, RandomColorFragment())
                    .commit()
            }
        }
    }
}
