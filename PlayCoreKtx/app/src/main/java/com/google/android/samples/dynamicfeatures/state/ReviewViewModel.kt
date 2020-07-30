package com.google.android.samples.dynamicfeatures.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewViewModel(private val reviewManager: ReviewManager) : ViewModel() {
    private var reviewInfo: ReviewInfo? = null

    fun preWarmReview() {
        viewModelScope.launch {
            reviewInfo = reviewManager.requestReview()
            Log.d("REVIEW", "got reviewinfo: $reviewInfo")
        }
    }

    suspend fun getReviewInfo(): ReviewInfo = withContext(Dispatchers.Main.immediate) {
        val info = reviewInfo ?: reviewManager.requestReview() // TODO this is wrong, can launch 2 requestreviews
        reviewInfo = null
        info
    }
}

class ReviewViewModelProviderFactory(
    private val manager: ReviewManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ReviewManager::class.java).newInstance(manager)
    }
}
