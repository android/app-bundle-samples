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

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class ReviewViewModel(private val reviewManager: ReviewManager) : ViewModel() {
    private var reviewInfo: Deferred<ReviewInfo>? = null

    @MainThread
    fun preWarmReview() {
        if (reviewInfo == null) {
            reviewInfo = viewModelScope.async { reviewManager.requestReview() }
        }
    }

    suspend fun obtainReviewInfo(): ReviewInfo? = withContext(Dispatchers.Main.immediate) {
        preWarmReview()
        return@withContext reviewInfo?.await().also {
            reviewInfo = null
        }
    }
}

class ReviewViewModelProviderFactory(
        private val manager: ReviewManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ReviewManager::class.java).newInstance(manager)
    }
}
