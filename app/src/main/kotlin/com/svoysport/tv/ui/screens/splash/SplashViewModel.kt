package com.svoysport.tv.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svoysport.tv.data.repository.ActivationRepository
import com.svoysport.tv.session.SubscriptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: ActivationRepository
) : ViewModel() {
    private val _validationComplete = MutableStateFlow(false)
    val validationComplete = _validationComplete.asStateFlow()

    init {
        viewModelScope.launch {
            repository.subscription(SubscriptionManager.deviceId.value)
                .onSuccess { subscription ->
                    if (subscription.active && subscription.until != null) {
                        SubscriptionManager.activate(subscription.until)
                    } else {
                        SubscriptionManager.clear()
                    }
                }
            _validationComplete.value = true
        }
    }
}
