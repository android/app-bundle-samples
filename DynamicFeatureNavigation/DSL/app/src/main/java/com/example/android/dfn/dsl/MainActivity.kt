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
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.dynamicfeatures.activity
import androidx.navigation.dynamicfeatures.createGraph
import androidx.navigation.dynamicfeatures.fragment.DynamicNavHostFragment
import androidx.navigation.dynamicfeatures.fragment.fragment
import androidx.navigation.dynamicfeatures.includeDynamic
import com.example.android.dfn.dsl.databinding.ActivityMainBinding

/**
 * Main entry point into the Kotlin DSL sample for Dynamic Feature Navigation.
 * The navigation graph is declared in this class.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)

        (supportFragmentManager.findFragmentByTag("nav_host") as DynamicNavHostFragment)
            .navController
            .apply {
                // Create a dynamic navigation graph. Inside this destinations from within dynamic
                // feature modules can be navigated to.
                graph = createGraph(
                    id = R.id.graphId,
                    startDestination = R.id.startDestination
                ) {
                    // A fragment destination declared in the "base" feature module.
                    fragment<StartDestinationFragment>(R.id.startDestination)
                    // A fragment destination declared in the "ondemand" dynamic feature module.
                    fragment(R.id.onDemandFragment, "${baseContext.packageName}$ON_DEMAND_FRAGMENT") {
                        moduleName = MODULE_ON_DEMAND
                    }
                    // An activity destination declared in the "ondemand" dynamic feature module.
                    activity(id = R.id.featureActivity) {
                        moduleName = MODULE_ON_DEMAND
                        activityClassName = "${baseContext.packageName}$FEATURE_ACTIVITY"
                    }
                    /* A dynamically included graph in the "included" dynamic feature module.
                       This matches the <include-dynamic> tag in xml. */
                    includeDynamic(
                        id = R.id.includedFragment,
                        moduleName = MODULE_INCLUDED,
                        graphResourceName = INCLUDED_GRAPH_NAME
                    )
                }
            }
    }
}

const val INCLUDED_GRAPH_NAME = "included_dynamically"

const val ON_DEMAND_FRAGMENT = ".ondemand.OnDemandFragment"
const val FEATURE_ACTIVITY = ".ondemand.FeatureActivity"

const val MODULE_INCLUDED = "included"
const val MODULE_ON_DEMAND = "ondemand"
