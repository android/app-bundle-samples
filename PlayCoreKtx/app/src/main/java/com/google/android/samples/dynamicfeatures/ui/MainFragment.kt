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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
class MainFragment : Fragment(R.layout.fragment_main) {

    private var bindings: FragmentMainBinding? = null

    private lateinit var splitInstallManager: SplitInstallManager
    private val installViewModel by viewModels<InstallViewModel> {
        InstallViewModelProviderFactory(splitInstallManager)
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateViewModel by viewModels<UpdateViewModel> {
        UpdateViewModelProviderFactory(appUpdateManager)
    }

    private lateinit var reviewManager: ReviewManager
    private val reviewViewModel by viewModels<ReviewViewModel> {
        ReviewViewModelProviderFactory(reviewManager)
    }

    private val colorViewModel by activityViewModels<ColorViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        splitInstallManager = SplitInstallManagerFactory.create(context)
        appUpdateManager = AppUpdateManagerFactory.create(context)
        reviewManager = ReviewManagerFactory.create(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindings = FragmentMainBinding.bind(view).apply {
            btnInvokePalette.setOnClickListener { installViewModel.invokePictureSelection() }
            btnUpdate.setOnClickListener {
                updateViewModel.invokeUpdate()
            }
        }

        addInstallViewModelObservers()
        addUpdateViewModelObservers()

        colorViewModel.backgroundColor.onEach {
            view.setBackgroundColor(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
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
                    updateModuleButton(it.btnInvokePalette, status)
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
                    viewLifecycleOwner, Observer { updateResult: AppUpdateResult ->
                updateUpdateButton(updateResult)
            })
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

    private fun updateModuleButton(target: View, status: ModuleStatus) {
        target.isEnabled = status !is ModuleStatus.Unavailable
        bindings?.moduleState?.apply {
            when (status) {
                ModuleStatus.Available -> {
                    text = getString(R.string.install)
                }
                is ModuleStatus.Installing -> {
                    text = getString(
                            R.string.installing,
                            (status.progress * 100).toInt()
                    )
                }
                ModuleStatus.Unavailable -> {
                    text = getString(R.string.feature_not_available)
                }
                ModuleStatus.Installed -> {
                    SplitCompat.installActivity(requireActivity())
                    text = getString(R.string.start)
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
        bindings?.let { bindings ->
            when (updateResult) {
                NotAvailable -> bindings.btnUpdate.visibility = View.GONE
                is Available -> {
                    with(bindings.btnUpdate) {
                        visibility = View.VISIBLE
                        isEnabled = true
                        text = context.getString(R.string.start_update)
                    }
                }
                is InProgress -> {
                    with(bindings.btnUpdate) {
                        visibility = View.VISIBLE
                        isEnabled = false
                        val updateProgress =
                                updateResult.installState.bytesDownloaded * 100 /
                                        updateResult.installState.totalBytesToDownload
                        text = context.getString(R.string.downloading_update, updateProgress)
                    }
                }
                is Downloaded -> {
                    with(bindings.btnUpdate) {
                        visibility = View.VISIBLE
                        isEnabled = true
                        text = context.getString(R.string.press_to_complete_update)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (colorViewModel.shouldLaunchReview) {
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                val reviewInfo = reviewViewModel.getReviewInfo()
                reviewManager.launchReview(requireActivity(), reviewInfo)
            }
        }
    }

    override fun onDestroyView() {
        bindings = null
        super.onDestroyView()
    }

    /** This is needed to handle the result of the manager.startConfirmationDialogForResult
    request that can be made from SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION
    in the listener above. */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // TODO might not be needed at all if we just get updates from Flow/LiveData???
        if (requestCode == INSTALL_CONFIRMATION_REQ_CODE) {
            // Handle the user's decision. For example, if the user selects "Cancel",
            // you may want to disable certain functionality that depends on the module.
            if (resultCode == Activity.RESULT_CANCELED) {
//                toastAndLog(getString(R.string.user_cancelled))
            }
        } else if (requestCode == UPDATE_CONFIRMATION_REQ_CODE) {
            // TODO Handle flexible updates by updating the UI
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}

fun MainFragment.toastAndLog(text: String) {
    Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    Log.d(TAG, text)
}

private const val TAG = "DynamicFeatures"
const val INSTALL_CONFIRMATION_REQ_CODE = 1
const val UPDATE_CONFIRMATION_REQ_CODE = 2
