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

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.requestUpdateFlow
import com.google.android.play.core.ktx.updatePriority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for InAppUpdates.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UpdateViewModel(manager: AppUpdateManager) : ViewModel() {

    private val _immediateUpdate = MutableLiveData<Event<Boolean>>()
    val immediateUpdate: LiveData<Event<Boolean>> = _immediateUpdate

    private val _flexibleUpdate = MutableLiveData<Event<Boolean>>()
    val flexibleUpdate: LiveData<Event<Boolean>> = _flexibleUpdate

    val updateStatus = manager.requestUpdateFlow().catch {
        toast("Update info not available")
    }.asLiveData()

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

    fun invokeUpdate(fragment: Fragment, requestCode: Int = UPDATE_CONFIRMATION_REQ_CODE) {
        when (val updateResult = updateStatus.value) {
            AppUpdateResult.NotAvailable -> toast("No update available")
            is AppUpdateResult.Available -> {
                with(updateResult.updateInfo) {
                    if ((clientVersionStalenessDays ?: 0) > 7 && isImmediateUpdateAllowed) {
                        updateResult.startImmediateUpdate(fragment, UPDATE_CONFIRMATION_REQ_CODE)
                    } else if (updatePriority > 4 && isImmediateUpdateAllowed) {
                        updateResult.startImmediateUpdate(fragment, UPDATE_CONFIRMATION_REQ_CODE)
                    } else if (isFlexibleUpdateAllowed) {
                        updateResult.startFlexibleUpdate(fragment, UPDATE_CONFIRMATION_REQ_CODE)
                    } else {
                        throw IllegalStateException("Not implemented: Handling for $this")
                    }
                }
            }
            is AppUpdateResult.InProgress -> toast("Update already in progress")
            is AppUpdateResult.Downloaded -> viewModelScope.launch {
                updateResult.completeUpdate()
            }
        }
    }

    private fun toast(message: String) {
        _toastMessage.value = Event(message)
    }
}

const val UPDATE_CONFIRMATION_REQ_CODE = 2

class UpdateViewModelProviderFactory(
    private val manager: AppUpdateManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(AppUpdateManager::class.java).newInstance(manager)
    }
}
