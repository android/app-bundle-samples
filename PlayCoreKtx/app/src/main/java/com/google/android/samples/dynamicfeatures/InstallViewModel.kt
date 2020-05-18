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
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.moduleNames
import com.google.android.play.core.ktx.requestInstall
import com.google.android.play.core.ktx.requestProgressFlow
import com.google.android.play.core.ktx.status
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InstallViewModel(val app: Application) : AndroidViewModel(app) {
    private val manager = SplitInstallManagerFactory.create(app)

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage
//
//    private val _navigationEvents = MutableLiveData<Event<String>>()
//    val navigationEvents: LiveData<Event<String>> = _navigationEvents

    val moduleStatus: LiveData<ModuleStatus> = manager.requestProgressFlow()
            .filter { state -> state.moduleNames.contains(PALETTE_MODULE) }
            .map { state ->
                Log.d("STATE", state.toString())
                when (state.status) {
                    SplitInstallSessionStatus.CANCELED -> ModuleStatus.Available
                    SplitInstallSessionStatus.CANCELING -> ModuleStatus.Installing(0.0)
                    SplitInstallSessionStatus.DOWNLOADED -> ModuleStatus.Installing(1.0)
                    SplitInstallSessionStatus.DOWNLOADING -> ModuleStatus.Installing(
                            state.bytesDownloaded.toDouble() / state.totalBytesToDownload
                    )
                    SplitInstallSessionStatus.FAILED -> {
                        _toastMessage.value = Event(app.getString(R.string.error_for_module, state.errorCode(),
                                state.moduleNames()))
                        ModuleStatus.Available
                    }
                    SplitInstallSessionStatus.INSTALLED -> ModuleStatus.Installed
                    SplitInstallSessionStatus.INSTALLING -> ModuleStatus.Installing(1.0)
                    SplitInstallSessionStatus.PENDING -> ModuleStatus.Installing(0.0)
                    SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> ModuleStatus.NeedsConfirmation(state)
                    SplitInstallSessionStatus.UNKNOWN -> ModuleStatus.Unavailable
                    else -> ModuleStatus.Unavailable
                }
            }.catch {
                _toastMessage.value = Event("Something went wrong. No install progress will be reported.")
                emit(ModuleStatus.Unavailable)
            }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun invokePalette() {
        if (manager.installedModules.contains(PALETTE_MODULE)) {
            // TODO invoke the Palette UI and do something
            _toastMessage.value = Event("Invoked Palette!")
        } else {
            if (moduleStatus.value is ModuleStatus.NeedsConfirmation) {
                // TODO invoke confirmation UI
            } else {
                viewModelScope.launch {
                    try {
                        manager.requestInstall(modules = listOf(PALETTE_MODULE))
                    } catch (e: SplitInstallException) {
                        _toastMessage.value = Event("Failed starting installation of $PALETTE_MODULE")
                    }
                }
            }
        }
    }
}

sealed class ModuleStatus {
    object Available : ModuleStatus()
    data class Installing(val progress: Double) : ModuleStatus()
    object Unavailable : ModuleStatus()
    object Installed : ModuleStatus()
    class NeedsConfirmation(val state: SplitInstallSessionState) : ModuleStatus()
}

const val PALETTE_MODULE = "palette"
