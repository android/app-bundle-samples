/*
 * Copyright 2020 Google LLC.
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
 */

package com.google.android.samples.dynamicnavigator

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.samples.dynamicnavigator.databinding.FragmentNavigateDynamicBinding

/**
 * Load and display navigation destinations in the simplest way for developers.
 * The dynamic feature navigation library takes care of handling states and monitoring installation.
 */
class DefaultDynamicNavigationFragment : Fragment(R.layout.fragment_navigate_dynamic) {

    private var viewBinding: FragmentNavigateDynamicBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = FragmentNavigateDynamicBinding.bind(view).also {
            val navController = findNavController()
            mapOf(
                Pair(it.feature, R.id.featureFragment),
                Pair(it.featureActivity, R.id.featureActivity),
                Pair(it.nestedGraph, R.id.nestedGraph),
                Pair(it.includedGraph, R.id.includedGraph)
            ).forEach { (targetView, targetId) ->
                targetView.setOnClickListener {
                    navController.navigate(targetId)
                }
            }
        }
    }

    override fun onDestroy() {
        viewBinding = null
        super.onDestroy()
    }
}
