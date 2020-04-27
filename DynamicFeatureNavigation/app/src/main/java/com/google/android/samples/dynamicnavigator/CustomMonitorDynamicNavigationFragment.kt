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
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.dynamicfeatures.DynamicExtras
import androidx.navigation.dynamicfeatures.DynamicInstallMonitor
import androidx.navigation.fragment.findNavController
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

/**
 * Load and display navigation destinations using a custom [DynamicInstallMonitor].
 * With this you can decide how the installation progress should be displayed to users.
 */
class CustomMonitorDynamicNavigationFragment : Fragment(R.layout.fragment_navigate_dynamic) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        mapOf(
            Pair(R.id.feature, R.id.featureFragment),
            Pair(R.id.feature_activity, R.id.featureActivity),
            Pair(R.id.nested_graph, R.id.nestedGraph),
            Pair(R.id.included_graph, R.id.includedGraph)
        ).forEach { (targetViewId, destinationId) ->
            view.findViewById<Button>(targetViewId).setOnClickListener { button ->
                val installMonitor = DynamicInstallMonitor()
                val dynamicExtras = DynamicExtras(installMonitor)

                navController.navigate(destinationId, null, null, dynamicExtras)

                if (installMonitor.isInstallRequired) {
                    observeInstallationState(installMonitor, button)
                }
            }
        }
    }

    private fun observeInstallationState(installMonitor: DynamicInstallMonitor, button: View?) {
        installMonitor.status.observe(
            viewLifecycleOwner,
            object : Observer<SplitInstallSessionState> {
                override fun onChanged(state: SplitInstallSessionState) {
                    (button as Button).text = getString(
                        when (state.status()) {
                            SplitInstallSessionStatus.INSTALLED -> R.string.launch
                            SplitInstallSessionStatus.FAILED -> R.string.installation_failed
                            else -> R.string.installing
                        }
                    )
                    if (state.hasTerminalStatus()) {
                        installMonitor.status.removeObserver(this)
                    }
                }
            })
    }
}