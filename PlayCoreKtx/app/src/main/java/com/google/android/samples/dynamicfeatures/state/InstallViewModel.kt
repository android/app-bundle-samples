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

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.google.android.samples.dynamicfeatures.state.ModuleStatus.Available
import com.google.android.samples.dynamicfeatures.state.ModuleStatus.Installed
import com.google.android.samples.dynamicfeatures.state.ModuleStatus.Installing
import com.google.android.samples.dynamicfeatures.state.ModuleStatus.NeedsConfirmation
import com.google.android.samples.dynamicfeatures.state.ModuleStatus.Unavailable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InstallViewModel(private val manager: SplitInstallManager) : ViewModel() {

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

    private val _errorMessage = MutableLiveData<Event<InstallError>>()
    val errorMessage: LiveData<Event<InstallError>> = _errorMessage

    private val _userConfirmationRequired = MutableLiveData<Event<NeedsConfirmation>>()
    val userConfirmationRequired = _userConfirmationRequired

    private val _activityIntent = MutableLiveData<Event<Intent>>()
    val activityIntent: LiveData<Event<Intent>> = _activityIntent

    val randomColorModuleStatus = getStatusLiveDataForModule(RANDOM_COLOR_MODULE)
    val pictureModuleStatus = getStatusLiveDataForModule(PICTURE_MODULE)

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
                    SplitInstallSessionStatus.DOWNLOADED -> Installing(1.0)
                    SplitInstallSessionStatus.DOWNLOADING -> Installing(
                        state.bytesDownloaded.toDouble() / state.totalBytesToDownload
                    )
                    SplitInstallSessionStatus.FAILED -> {
                        _errorMessage.value =
                            Event(InstallError(state.errorCode(), state.moduleNames.toString()))
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
                _toastMessage.value =
                    Event(
                        "Something went wrong. No install progress will be reported."
                    )
                emit(Unavailable)
            }.asLiveData()
    }

    fun invokeRandomColor() {
        openActivityInOnDemandModule(
            RANDOM_COLOR_MODULE,
            "com.google.android.samples.dynamicfeatures.ondemand.RandomColorActivity"
        )
    }

    fun invokePictureSelection() {
        openActivityInOnDemandModule(
            PICTURE_MODULE,
            "com.google.android.samples.dynamicfeatures.ondemand.PictureActivity"
        )
    }

    private fun openActivityInOnDemandModule(moduleName: String, activityName: String) {
        if (manager.installedModules.contains(moduleName)) {
            _activityIntent.value = Event(
                Intent().apply {
                    setClassName(
                        "com.google.android.samples.playcore",
                        activityName
                    )
                    flags = FLAG_ACTIVITY_NEW_TASK
                })
        } else {
            val status = when (moduleName) {
                RANDOM_COLOR_MODULE -> randomColorModuleStatus.value
                PICTURE_MODULE -> pictureModuleStatus.value
                else -> throw IllegalArgumentException("State not implemented")
            }
            if (status is NeedsConfirmation) {
                if (!_userConfirmationRequired.hasActiveObservers())
                    _toastMessage.value = Event(
                        "Make sure to register an observer for user confirmation!"
                    )
                _userConfirmationRequired.value = Event(status)
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
                _toastMessage.value = Event("Failed starting installation of $moduleName")
            }
        }
    }

    fun startConfirmationDialogForResult(
        state: SplitInstallSessionState,
        activity: Activity,
        requestCode: Int = INSTALL_CONFIRMATION_REQ_CODE
    ) = manager.startConfirmationDialogForResult(state, activity, requestCode)
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

const val RANDOM_COLOR_MODULE = "randomcolor"
const val PICTURE_MODULE = "picture"
const val INSTALL_CONFIRMATION_REQ_CODE = 1
