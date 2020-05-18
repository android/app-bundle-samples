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
import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
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
import com.google.android.samples.dynamicfeatures.R.string
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
import java.lang.IllegalArgumentException

@OptIn(ExperimentalCoroutinesApi::class)
class InstallViewModel(val app: Application) : AndroidViewModel(app) {
    private val manager = SplitInstallManagerFactory.create(app)

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

//    private val _navigationEvents = MutableLiveData<Event<String>>()
//    val navigationEvents: LiveData<Event<String>> = _navigationEvents

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
                        _toastMessage.value =
                            Event(
                                app.getString(
                                    string.error_for_module,
                                    state.errorCode(),
                                    state.moduleNames()
                                )
                            )
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
            startActivity(activityName)
            _toastMessage.value = Event("Invoked $moduleName")
        } else {
            if (when (moduleName) {
                    RANDOM_COLOR_MODULE -> randomColorModuleStatus.value
                    PICTURE_MODULE -> pictureModuleStatus.value
                    else -> throw IllegalArgumentException("State not implemented")
                } is NeedsConfirmation
            ) {
                _toastMessage.value = Event("Confirmation required (Not Implemented)")
                TODO("Invoke confirmation UI")
            } else {
                requestModuleInstallation(moduleName)
            }
        }
    }

    private fun requestModuleInstallation(moduleName: String) {
        viewModelScope.launch {
            try {
                manager.requestInstall(
                    modules = listOf(
                        moduleName
                    )
                )
            } catch (e: SplitInstallException) {
                _toastMessage.value =
                    Event(
                        "Failed starting installation of $moduleName"
                    )
            }
        }
    }

    private fun startActivity(activityFQCN: String) {
        app.startActivity(
            Intent(app, Class.forName(activityFQCN)).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
            })
    }

    fun startConfirmationDialogForResult(
        state: SplitInstallSessionState,
        activity: Activity,
        requestCode: Int
    ) =
        manager.startConfirmationDialogForResult(state, activity, requestCode)
}

sealed class ModuleStatus {
    object Available : ModuleStatus()
    data class Installing(val progress: Double) : ModuleStatus()
    object Unavailable : ModuleStatus()
    object Installed : ModuleStatus()
    class NeedsConfirmation(val state: SplitInstallSessionState) : ModuleStatus()
}

const val RANDOM_COLOR_MODULE = "randomcolor"
const val PICTURE_MODULE = "picture"
