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
package com.google.android.samples.dynamicfeatures.ui

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.AppUpdateResult.Available
import com.google.android.play.core.ktx.AppUpdateResult.Downloaded
import com.google.android.play.core.ktx.AppUpdateResult.InProgress
import com.google.android.play.core.ktx.AppUpdateResult.NotAvailable
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.startConfirmationDialogForResult
import com.google.android.play.core.ktx.startUpdateFlowForResult
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.samples.dynamicfeatures.R
import com.google.android.samples.dynamicfeatures.databinding.FragmentMainBinding
import com.google.android.samples.dynamicfeatures.state.ColorViewModel
import com.google.android.samples.dynamicfeatures.state.Event
import com.google.android.samples.dynamicfeatures.state.InstallViewModel
import com.google.android.samples.dynamicfeatures.state.InstallViewModelProviderFactory
import com.google.android.samples.dynamicfeatures.state.ModuleStatus
import com.google.android.samples.dynamicfeatures.state.ReviewViewModel
import com.google.android.samples.dynamicfeatures.state.ReviewViewModelProviderFactory
import com.google.android.samples.dynamicfeatures.state.UpdateViewModel
import com.google.android.samples.dynamicfeatures.state.UpdateViewModelProviderFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Keep
class MainFragment : Fragment(R.layout.fragment_main) {

    private var bindings: FragmentMainBinding? = null

    private lateinit var splitInstallManager: SplitInstallManager
    private val installViewModel by viewModels<InstallViewModel> {
        InstallViewModelProviderFactory(splitInstallManager)
    }
    private var startModuleWhenReady: Boolean = false


    private lateinit var appUpdateManager: AppUpdateManager
    private val updateViewModel by viewModels<UpdateViewModel> {
        UpdateViewModelProviderFactory(appUpdateManager)
    }

    private lateinit var reviewManager: ReviewManager
    private val reviewViewModel by activityViewModels<ReviewViewModel> {
        ReviewViewModelProviderFactory(reviewManager)
    }

    private val colorViewModel by activityViewModels<ColorViewModel>()

    private lateinit var snackbar: Snackbar

    override fun onAttach(context: Context) {
        super.onAttach(context)
        splitInstallManager = SplitInstallManagerFactory.create(context)
        appUpdateManager = AppUpdateManagerFactory.create(context)
        reviewManager = ReviewManagerFactory.create(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindings = FragmentMainBinding.bind(view).apply {
            btnInvokePalette.setOnClickListener {
                startModuleWhenReady = true
                installViewModel.invokePictureSelection()
            }

            btnToggleLight.setOnClickListener {
                startModuleWhenReady = false
                colorViewModel.lightsOn.value = !colorViewModel.lightsOn.value
            }

            val colorBackgroundOff = ContextCompat.getColor(requireContext(), R.color.background)
            val drawable = btnToggleLight.drawable as AnimatedVectorDrawable
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                colorViewModel.lightsOn
                        .onEach { lightsOn: Boolean ->
                            if (lightsOn) {
                                // The user has turned on the flashlight.
                                drawable.start()
                                view.setBackgroundColor(colorViewModel.backgroundColor.value)
                            } else {
                                // The user has turned off the flashlight. Reset the icon and color:
                                drawable.reset()
                                view.setBackgroundColor(colorBackgroundOff)
                            }
                        }
                        .drop(1) // for launching the review ignore the starting value
                        .collect { lightsOn ->
                            // Check if the color was picked from a photo,
                            // and launch a review if yes:
                            if (!lightsOn && colorViewModel.colorWasPicked) {
                                colorViewModel.notifyPickedColorConsumed()
                                val reviewInfo = reviewViewModel.obtainReviewInfo()
                                if (reviewInfo != null) {
                                    reviewManager.launchReview(requireActivity(), reviewInfo)
                                    reviewViewModel.notifyAskedForReview()
                                }
                            }
                        }
            }
        }

        snackbar = Snackbar.make(view, R.string.update_available, Snackbar.LENGTH_INDEFINITE)

        addInstallViewModelObservers()
        addUpdateViewModelObservers()
    }

    private fun addInstallViewModelObservers() {
        with(installViewModel) {
            events.onEach { event ->
                when (event) {
                    is Event.ToastEvent -> toastAndLog(event.message)
                    is Event.NavigationEvent -> {
                        navigateToFragment(event.fragmentClass)
                    }
                    is Event.InstallConfirmationEvent -> splitInstallManager.startConfirmationDialogForResult(
                            event.status,
                            this@MainFragment,
                            INSTALL_CONFIRMATION_REQ_CODE
                    )
                    else -> throw IllegalStateException("Event type not handled: $event")
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
            pictureModuleStatus.observe(viewLifecycleOwner, Observer { status ->
                bindings?.let {
                    updateModuleButton(status)
                }
            })
        }
    }

    private fun navigateToFragment(fragmentClass: String) {
        val fragment = parentFragmentManager.fragmentFactory.instantiate(
                ClassLoader.getSystemClassLoader(),
                fragmentClass)
        parentFragmentManager.beginTransaction()
                .replace(R.id.mycontainer, fragment)
                .addToBackStack(null)
                .commit()
    }

    private fun addUpdateViewModelObservers() {
        with(updateViewModel) {
            updateStatus.observe(
                    viewLifecycleOwner,
                    { updateResult: AppUpdateResult ->
                        updateUpdateButton(updateResult)

                        // If it's an immediate update, launch it immediately and finish Activity
                        // to prevent the user from using the app until they update.
                        if (updateResult is Available) {
                            if (shouldLaunchImmediateUpdate(updateResult.updateInfo)) {
                                if (appUpdateManager.startUpdateFlowForResult(
                                        updateResult.updateInfo,
                                        AppUpdateType.IMMEDIATE,
                                        this@MainFragment,
                                        UPDATE_CONFIRMATION_REQ_CODE
                                )) {
                                    // only exit if update flow really started
                                    requireActivity().finish()
                                }
                            }
                        }
                    }
            )
            events.onEach { event ->
                when (event) {
                    is Event.ToastEvent -> toastAndLog(event.message)
                    is Event.StartUpdateEvent -> {
                        val updateType = if (event.immediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
                        appUpdateManager.startUpdateFlowForResult(
                                event.updateInfo,
                                updateType,
                                this@MainFragment,
                                UPDATE_CONFIRMATION_REQ_CODE
                        )
                    }
                    else -> throw IllegalStateException("Event type not handled: $event")
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
    }

    private fun updateModuleButton(status: ModuleStatus) {
        bindings?.btnInvokePalette?.apply {
            isEnabled = status !is ModuleStatus.Unavailable
            when (status) {
                ModuleStatus.Available -> {
                    text = getString(R.string.install)
                    shrink()
                }
                is ModuleStatus.Installing -> {
                    text = getString(
                            R.string.installing,
                            (status.progress * 100).toInt()
                    )
                    extend()
                }
                ModuleStatus.Unavailable -> {
                    text = getString(R.string.feature_not_available)
                    shrink()
                }
                ModuleStatus.Installed -> {
                    SplitCompat.installActivity(requireActivity())
                    shrink()
                    if (startModuleWhenReady) {
                        startModuleWhenReady = false
                        installViewModel.invokePictureSelection()
                    }
                }
                is ModuleStatus.NeedsConfirmation -> {
                    splitInstallManager.startConfirmationDialogForResult(
                            status.state,
                            this@MainFragment,
                            UPDATE_CONFIRMATION_REQ_CODE
                    )
                }
            }
        }
    }

    private fun updateUpdateButton(updateResult: AppUpdateResult) {
        when (updateResult) {
            NotAvailable -> {
                Log.d(TAG, "No update available")
                snackbar.dismiss()
            }
            is Available -> with(snackbar) {
                setText(R.string.update_available_snackbar)
                setAction(R.string.update_now) {
                    updateViewModel.invokeUpdate()
                }
                show()
            }
            is InProgress -> {
                with(snackbar) {
                    val updateProgress: Int = if (updateResult.installState.totalBytesToDownload == 0L) {
                        0
                    } else {
                        (updateResult.installState.bytesDownloaded * 100 /
                                updateResult.installState.totalBytesToDownload).toInt()
                    }
                    setText(context.getString(R.string.downloading_update, updateProgress))
                    setAction(null) {}
                    show()
                }
            }
            is Downloaded -> {
                with(snackbar) {
                    setText(R.string.update_downloaded)
                    setAction(R.string.complete_update) {
                        updateViewModel.invokeUpdate()
                    }
                    show()
                }
            }
        }
    }

    override fun onDestroyView() {
        bindings = null
        super.onDestroyView()
    }
}

fun MainFragment.toastAndLog(text: String) {
    Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    Log.d(TAG, text)
}

private const val TAG = "DynamicFeatures"
const val INSTALL_CONFIRMATION_REQ_CODE = 1
const val UPDATE_CONFIRMATION_REQ_CODE = 2
