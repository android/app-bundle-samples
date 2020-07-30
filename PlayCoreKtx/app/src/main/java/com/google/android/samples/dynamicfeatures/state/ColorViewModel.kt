package com.google.android.samples.dynamicfeatures.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
class ColorViewModel : ViewModel() {
    val backgroundColor = MutableStateFlow(0)
    var ignoreFirstValue = true

    init {
        backgroundColor.onEach {
            if (ignoreFirstValue) {
                ignoreFirstValue = false
            } else {
                shouldLaunchReview = true
            }
        }.launchIn(viewModelScope)
    }

    var shouldLaunchReview: Boolean = false
        get() {
            val value = field
            field = false
            return value
        }
        private set(value) { field = value }
}