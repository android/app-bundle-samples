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

import androidx.annotation.Keep
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class ReviewViewModel @Keep constructor(private val reviewManager: ReviewManager) : ViewModel() {
    /**
     * For this sample, we check if the user has been asked to review during this application session
     * already. Every time the app process starts fresh, it will be reset.
     *
     * In a real app, you should implement your own back-off strategy, for example:
     * you could persist the last time the user was asked in a database,
     * and only ask if at least a week has passed from the previous request.
     */
    private var alreadyAskedForReview = false
    private var reviewInfo: Deferred<ReviewInfo>? = null

    /**
     * Start requesting the review info that will be needed later in advance.
     */
    @MainThread
    fun preWarmReview() {
        if (!alreadyAskedForReview && reviewInfo == null) {
            reviewInfo = viewModelScope.async { reviewManager.requestReview() }
        }
    }

    /**
     * Only return ReviewInfo object if the prewarming has already completed,
     * i.e. if the review can be launched immediately.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun obtainReviewInfo(): ReviewInfo? = withContext(Dispatchers.Main.immediate) {
        if (reviewInfo?.isCompleted == true && reviewInfo?.isCancelled == false) {
            reviewInfo?.getCompleted().also {
                reviewInfo = null
            }
        } else null
    }

    /**
     * The view should call this to let the ViewModel know that an attemt to show the review dialog
     * was made.
     *
     * A real app could record the time when this request was made to implement a back-off strategy.
     *
     * @see alreadyAskedForReview
     */
    fun notifyAskedForReview() {
        alreadyAskedForReview = true
    }
}

class ReviewViewModelProviderFactory(
        private val manager: ReviewManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ReviewManager::class.java).newInstance(manager)
    }
}
