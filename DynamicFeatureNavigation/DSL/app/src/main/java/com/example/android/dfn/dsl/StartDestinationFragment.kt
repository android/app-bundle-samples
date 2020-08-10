/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.dfn.dsl

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.android.dfn.dsl.databinding.StartDestinationFragmentBinding

class StartDestinationFragment : Fragment(R.layout.start_destination_fragment) {

    private var _binding: StartDestinationFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        _binding = StartDestinationFragmentBinding.inflate(layoutInflater, container, false)
        setUpNavigation()
        return binding.root
    }

    private fun setUpNavigation() {
        Log.d("DFN", "Navigation created")
        with(binding) {
            navigateToOnDemandFragment.setNavigationDestination(R.id.onDemandFragment)
            navigateToIncludedGraph.setNavigationDestination(R.id.includedFragment)
            navigateToFeatureActivity.setNavigationDestination(R.id.featureActivity)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Button.setNavigationDestination(@IdRes id: Int) {
        setOnClickListener {
            findNavController().navigate(id)
        }
    }

    companion object {
        private const val TAG = "StartDestinationFragmen"
    }
}
