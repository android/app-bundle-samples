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
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.moduleNames
import com.google.android.play.core.ktx.requestInstall
import com.google.android.play.core.ktx.requestProgressFlow
import com.google.android.play.core.ktx.status
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.google.android.samples.dynamicfeatures.state.Event.InstallConfirmationEvent
import com.google.android.samples.dynamicfeatures.state.Event.InstallErrorEvent
import com.google.android.samples.dynamicfeatures.state.Event.NavigationEvent
import com.google.android.samples.dynamicfeatures.state.Event.ToastEvent
import com.google.android.samples.dynamicfeatures.state.ModuleStatus.Available
import com.google.android.samples.dynamicfeatures.state.ModuleStatus.Installed
import com.google.android.samples.dynamicfeatures.state.ModuleStatus.Installing
import com.google.android.samples.dynamicfeatures.state.ModuleStatus.NeedsConfirmation
import com.google.android.samples.dynamicfeatures.state.ModuleStatus.Unavailable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class InstallViewModel @Keep constructor(private val manager: SplitInstallManager) : ViewModel() {

    val pictureModuleStatus = getStatusLiveDataForModule(PICTURE_MODULE)

    private val _events: BroadcastChannel<Event> = BroadcastChannel(Channel.BUFFERED)
    val events: Flow<Event> = _events.asFlow()

    private fun getStatusLiveDataForModule(moduleName: String): LiveData<ModuleStatus> {
        return manager.requestProgressFlow()
                .filter { state ->
                    state.moduleNames.contains(moduleName)
                }
                .map { state ->
                    Log.d("STATE", state.toString())
                    when (state.status) {
                        SplitInstallSessionStatus.CANCELED -> Available
                        SplitInstallSessionStatus.CANCELING -> Installing(0.0)
                        SplitInstallSessionStatus.DOWNLOADING -> Installing(
                                state.bytesDownloaded.toDouble() / state.totalBytesToDownload
                        )
                        SplitInstallSessionStatus.DOWNLOADED -> Installed
                        SplitInstallSessionStatus.FAILED -> {
                            _events.send(InstallErrorEvent(state))
                            Available
                        }
                        SplitInstallSessionStatus.INSTALLED -> Installed
                        SplitInstallSessionStatus.INSTALLING -> Installing(1.0)
                        SplitInstallSessionStatus.PENDING -> Installing(0.0)
                        SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> NeedsConfirmation(
                                state
                        )
                        SplitInstallSessionStatus.UNKNOWN -> Unavailable
                        else -> Unavailable
                    }
                }.catch {
                    _events.send(ToastEvent(
                            "Something went wrong. No install progress will be reported."
                    ))
                    emit(Unavailable)
                }.asLiveData()
    }

    fun invokePictureSelection() {
        openActivityInOnDemandModule(
                PICTURE_MODULE,
                "com.google.android.samples.dynamicfeatures.ondemand.PaletteFragment"
        )
    }

    private fun openActivityInOnDemandModule(moduleName: String, fragmentName: String) {
        if (manager.installedModules.contains(moduleName)) {
            viewModelScope.launch {
                _events.send(NavigationEvent(fragmentName))
            }
        } else {
            val status = when (moduleName) {
                PICTURE_MODULE -> pictureModuleStatus.value
                else -> throw IllegalArgumentException("State not implemented")
            }
            if (status is NeedsConfirmation) {
                viewModelScope.launch {
                    _events.send(InstallConfirmationEvent(status.state))
                }
            } else {
                requestModuleInstallation(moduleName)
            }
        }
    }

    private fun requestModuleInstallation(moduleName: String) {
        viewModelScope.launch {
            try {
                manager.requestInstall(listOf(moduleName))
            } catch (e: SplitInstallException) {
                _events.send(ToastEvent("Failed starting installation of $moduleName"))
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

class InstallViewModelProviderFactory(
        private val manager: SplitInstallManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(SplitInstallManager::class.java).newInstance(manager)
    }
}

const val PICTURE_MODULE = "picture"
