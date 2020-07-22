package com.google.android.samples.dynamicfeatures.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.launch

class ReviewViewModel(private val reviewManager: ReviewManager) : ViewModel() {
    private val _reviewInfo = MutableLiveData<ReviewInfo>()
    val reviewInfo: LiveData<ReviewInfo> = _reviewInfo

    init {
        viewModelScope.launch {
            val info = reviewManager.requestReview()
            _reviewInfo.value = info
        }
    }
}

class ReviewViewModelProviderFactory(
    private val manager: ReviewManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ReviewManager::class.java).newInstance(manager)
    }
}
