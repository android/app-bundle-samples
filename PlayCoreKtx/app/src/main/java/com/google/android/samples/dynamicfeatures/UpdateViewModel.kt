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
package com.google.android.samples.dynamicfeatures

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.requestUpdateFlow
import com.google.android.play.core.ktx.updatePriority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class UpdateViewModel(val app: Application) : AndroidViewModel(app) {
    private val manager = AppUpdateManagerFactory.create(app)

    @OptIn(ExperimentalCoroutinesApi::class)
    val updateStatus = manager.requestUpdateFlow().catch {
        toast("Update info not available")
    }.asLiveData()

    val toastMessage = MutableLiveData<Event<String>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun invokeUpdate() {
        when (val updateResult = updateStatus.value) {
            AppUpdateResult.NotAvailable -> toast("No update available")
            is AppUpdateResult.Available -> {
                with(updateResult.updateInfo) {
                    if ((clientVersionStalenessDays ?: 0) > 7 && isImmediateUpdateAllowed) {
                        // TODO updateResult.startImmediateUpdate()
                    } else if (updatePriority > 4 && isImmediateUpdateAllowed) {
                        // TODO updateResult.startImmediateUpdate()
                    } else if (isFlexibleUpdateAllowed) {
                        // TODO updateResult.startFlexibleUpdate()
                    }
                }
            }
            is AppUpdateResult.InProgress -> toast("Update already in progress")
            is AppUpdateResult.Downloaded -> viewModelScope.launch { updateResult.completeUpdate() }
        }
    }

    private fun toast(message: String) {
        toastMessage.value = Event(message)
    }
}
