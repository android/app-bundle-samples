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
import com.google.android.samples.dynamicnavigator.databinding.FragmentMainBinding

/** The primary fragment displaying navigation monitoring options. */
class MainFragment : Fragment(R.layout.fragment_main) {

    private var viewBinding: FragmentMainBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        viewBinding = FragmentMainBinding.bind(view).also {
            it.defaultMonitor.setOnClickListener {
                navController.navigate(R.id.defaultMonitorFragment)
            }
            it.customMonitor.setOnClickListener {
                navController.navigate(R.id.customMonitorFragment)
            }
        }
    }

    override fun onDestroy() {
        viewBinding = null
        super.onDestroy()
    }
}
