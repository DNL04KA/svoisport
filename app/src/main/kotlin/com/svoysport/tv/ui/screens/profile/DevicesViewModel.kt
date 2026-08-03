package com.svoysport.tv.ui.screens.profile

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
class DevicesViewModel @Inject constructor(private val repository: ActivationRepository) : ViewModel() {
    private val _devices = MutableStateFlow<List<DeviceItem>>(emptyList())
    val devices = _devices.asStateFlow()
    init { refresh() }
    fun refresh() = viewModelScope.launch {
        repository.devices(SubscriptionManager.deviceId.value).onSuccess { list ->
            _devices.value = list.map { DeviceItem(it.id, it.name, it.lastSeen ?: "Недавно", it.isCurrent) }
        }
    }
    fun disconnect(target: String? = null, allOthers: Boolean = false) = viewModelScope.launch {
        repository.disconnect(SubscriptionManager.deviceId.value, target, allOthers).onSuccess { refresh() }
    }

    fun disconnectCurrent(onComplete: () -> Unit) = viewModelScope.launch {
        val currentDeviceId = SubscriptionManager.deviceId.value
        repository.disconnect(currentDeviceId, currentDeviceId)
        onComplete()
    }
}
