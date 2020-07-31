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
