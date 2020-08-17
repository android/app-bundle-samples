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
package com.google.android.samples.dynamicfeatures.state

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.requestUpdateFlow
import com.google.android.play.core.ktx.updatePriority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for InAppUpdates.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class UpdateViewModel @Keep constructor(manager: AppUpdateManager) : ViewModel() {
    val updateStatus = manager.requestUpdateFlow()
            .catch {
                _events.send(Event.ToastEvent("Update info not available"))
            }
            .asLiveData()

    private val _events = BroadcastChannel<Event>(Channel.BUFFERED)
    val events = _events.asFlow()

    fun shouldLaunchImmediateUpdate(updateInfo: AppUpdateInfo): Boolean {
        with(updateInfo) {
            return isImmediateUpdateAllowed
                    &&
                    (clientVersionStalenessDays ?: 0 > 30
                            || updatePriority > 4)
        }
    }

    fun invokeUpdate() {
        when (val updateResult = updateStatus.value) {
            AppUpdateResult.NotAvailable -> viewModelScope.launch {
                _events.send(Event.ToastEvent("No update available"))
            }
            is AppUpdateResult.Available -> {
                with(updateResult.updateInfo) {
                    Log.d(TAG, "Update priority: $updatePriority")
                    when {
                        shouldLaunchImmediateUpdate(this) -> {
                            viewModelScope.launch {
                                _events.send(Event.StartUpdateEvent(updateResult.updateInfo, true))
                            }
                        }
                        isFlexibleUpdateAllowed -> {
                            viewModelScope.launch {
                                _events.send(Event.StartUpdateEvent(updateResult.updateInfo, false))
                            }
                        }
                        else -> {
                            throw IllegalStateException("Not implemented: Handling for $this")
                        }
                    }
                }
            }
            is AppUpdateResult.InProgress -> viewModelScope.launch {
                _events.send(Event.ToastEvent("Update already in progress"))
            }
            is AppUpdateResult.Downloaded -> viewModelScope.launch {
                updateResult.completeUpdate()
            }
        }
    }
}

class UpdateViewModelProviderFactory(
        private val manager: AppUpdateManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(AppUpdateManager::class.java).newInstance(manager)
    }
}

const val TAG = "UpdateViewModel"