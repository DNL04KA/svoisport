package com.svoysport.tv.ui.screens.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svoysport.tv.data.remote.activation.ActivationStatus
import com.svoysport.tv.data.repository.ActivationRepository
import com.svoysport.tv.session.SubscriptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

sealed interface ActivationUi {
    data object Loading : ActivationUi
    data class Qr(val qrUrl: String, val planId: String?) : ActivationUi
    data class Success(val until: String) : ActivationUi
    data class Error(val message: String) : ActivationUi
}

/**
 * Создаёт сессию активации, показывает QR и опрашивает статус каждые ~3 сек.
 * При ACTIVATED сохраняет подписку в [SubscriptionManager] и переходит в Success.
 */
@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val repo: ActivationRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ActivationUi>(ActivationUi.Loading)
    val state: StateFlow<ActivationUi> = _state.asStateFlow()

    private var pollJob: Job? = null
    private var currentPlanId: String? = null

    fun start(planId: String? = currentPlanId) {
        currentPlanId = planId
        pollJob?.cancel()
        _state.value = ActivationUi.Loading
        viewModelScope.launch {
            val deviceId = SubscriptionManager.deviceId.value
            repo.startSession(deviceId, planId)
                .onSuccess { session ->
                    _state.value = ActivationUi.Qr(session.qrUrl, planId)
                    pollJob = launch { pollLoop(session.sessionId, deviceId) }
                }
                .onFailure { e ->
                    _state.value = ActivationUi.Error(e.message ?: "Не удалось создать сессию активации")
                }
        }
    }

    private suspend fun pollLoop(sessionId: String, deviceId: String) {
        var failures = 0
        var polls = 0
        while (polls < MAX_POLLS) {
            delay(POLL_INTERVAL_MS)
            polls++
            repo.pollStatus(sessionId)
                .onSuccess { status ->
                    failures = 0
                    when (status) {
                        ActivationStatus.ACTIVATED -> {
                            val until = repo.subscription(deviceId).getOrNull()?.until ?: defaultUntil()
                            SubscriptionManager.activate(until)
                            // Активация = вход: наверху появляется иконка профиля
                            com.svoysport.tv.session.SessionManager.isLoggedIn.value = true
                            _state.value = ActivationUi.Success(until)
                            return
                        }
                        ActivationStatus.EXPIRED -> {
                            _state.value = ActivationUi.Error("Время сессии истекло. Попробуйте снова.")
                            return
                        }
                        ActivationStatus.WAITING -> { /* продолжаем опрос */ }
                    }
                }
                .onFailure {
                    // Терпим временные сетевые сбои, но не бесконечно.
                    if (++failures >= MAX_FAILURES) {
                        _state.value = ActivationUi.Error("Нет связи с сервером активации. Проверьте интернет.")
                        return
                    }
                }
        }
        _state.value = ActivationUi.Error("Время сессии истекло. Создайте новый QR-код.")
    }

    override fun onCleared() {
        pollJob?.cancel()
    }

    private fun defaultUntil(): String {
        val cal = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }
        return SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(cal.time)
    }

    private companion object {
        const val POLL_INTERVAL_MS = 3_000L
        const val MAX_FAILURES = 5
        const val MAX_POLLS = 15 * 60 * 1000 / POLL_INTERVAL_MS.toInt()
    }
}
