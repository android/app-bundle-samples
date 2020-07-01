/*
 * Copyright 2019 Google LLC
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
 *
 */
package com.google.android.samples.dynamiccodeloading

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

const val STORAGE_MODULE = "storage"

/**
 * The ViewModel for our single screen in the app.
 *
 * This is shared across the 3 flavors,
 * that's why we have to keep it clear of any Dagger bits for simplicity.
 *
 * It needs subclassing to override the initializeStorageFeature() method,
 * according to the chosen flavor's dynamic code loading mechanism.
 */
abstract class AbstractMainViewModel(app: Application) : AndroidViewModel(app) {
    private val splitInstallManager = SplitInstallManagerFactory.create(getApplication())
    private var sessionId = 0

    private val _counter = MutableLiveData<Int>()
    val counter: LiveData<Int> = _counter

    var storageModule: StorageFeature? = null

    private val listener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == sessionId) {
            when (state.status()) {
                SplitInstallSessionStatus.FAILED -> {
                    Log.d(TAG, "Module install failed with ${state.errorCode()}")
                    Toast.makeText(getApplication(), "Module install failed with ${state.errorCode()}", Toast.LENGTH_SHORT).show()
                }
                SplitInstallSessionStatus.INSTALLED -> {
                    Toast.makeText(getApplication(), "Storage module installed successfully", Toast.LENGTH_SHORT).show()
                    saveCounter()
                }
                else -> Log.d(TAG, "Status: ${state.status()}")
            }
        }
    }

    init {
        splitInstallManager.registerListener(listener)
    }

    override fun onCleared() {
        splitInstallManager.unregisterListener(listener)
        super.onCleared()
    }

    fun incrementCounter() {
        _counter.value = (_counter.value ?: 0) + 1
    }

    fun loadCounter() {
        if (isStorageInstalled()) {
            initializeStorageFeature()
        }
        _counter.value = storageModule?.loadCounter() ?: 0
    }

    fun saveCounter() {
        if (storageModule == null) {
            if (isStorageInstalled()) {
                initializeStorageFeature()
            } else {
                requestStorageInstall()
            }
        }
        if (storageModule != null) {
            storageModule?.saveCounter(_counter.value ?: 0)
            Toast.makeText(getApplication(), "Counter saved to storage", Toast.LENGTH_SHORT).show()
        }
    }

    protected abstract fun initializeStorageFeature()

    private fun isStorageInstalled() =
        if (BuildConfig.DEBUG) true else splitInstallManager.installedModules.contains(STORAGE_MODULE)

    private fun requestStorageInstall() {
        Toast.makeText(getApplication(), "Requesting storage module install", Toast.LENGTH_SHORT).show()
        val request =
            SplitInstallRequest
                .newBuilder()
                .addModule("storage")
                .build()

        splitInstallManager
            .startInstall(request)
            .addOnSuccessListener { id -> sessionId = id }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error installing module: ", exception)
                Toast.makeText(getApplication(), "Error requesting module install", Toast.LENGTH_SHORT).show()
            }
    }
}
