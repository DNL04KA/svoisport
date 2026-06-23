package com.svoysport.tv.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ScheduleUiState(
    val days: List<ScheduleDay> = emptyList(),
    val matchesByDay: Map<Int, List<ScheduleMatch>> = emptyMap(),
    val selectedDayId: Int = 0,
    val isLoading: Boolean = true,   // загрузка матчей выбранного дня
    val error: String? = null
)

/**
 * Расписание тянет данные по дням с sport-tv.by: `list.php?date=<unix>`
 * отдаёт трансляции конкретного дня. Дни строятся как текущая неделя Пн–Вс,
 * матчи каждого дня грузятся лениво при выборе и кэшируются в репозитории.
 */
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState())
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init { buildDaysAndLoadToday() }

    fun selectDay(id: Int) {
        _state.value = _state.value.copy(selectedDayId = id)
        loadDay(id)
    }

    private fun buildDaysAndLoadToday() {
        val dayFmt  = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        val shortFmt = SimpleDateFormat("d MMM", Locale("ru"))

        val weekStart = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val todayMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val days = mutableListOf<ScheduleDay>()
        var todayId = 0
        for (i in 0 until 7) {
            val dayStart = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }.timeInMillis
            val isToday = dayStart == todayMidnight
            if (isToday) todayId = i
            days += ScheduleDay(
                id = i,
                shortName = WEEK_SHORT[i],
                fullName = WEEK_FULL[i],
                date = shortFmt.format(Date(dayStart)),
                fullDate = dayFmt.format(Date(dayStart)),
                isToday = isToday,
                startMs = dayStart
            )
        }
        _state.value = _state.value.copy(days = days, selectedDayId = todayId)
        loadDay(todayId)
    }

    private fun loadDay(id: Int) {
        val day = _state.value.days.getOrNull(id) ?: return
        if (_state.value.matchesByDay.containsKey(id)) {
            _state.value = _state.value.copy(isLoading = false, error = null)
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getScheduleForDay(day.startMs)
                .onSuccess { matches ->
                    val rows = matches.map { it.toScheduleMatch() }
                    _state.value = _state.value.copy(
                        matchesByDay = _state.value.matchesByDay + (id to rows),
                        isLoading = false, error = null
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Не удалось загрузить расписание"
                    )
                }
        }
    }

    private fun MatchItem.toScheduleMatch(): ScheduleMatch {
        val timeFmt = SimpleDateFormat("HH:mm", Locale("ru"))
        val now = System.currentTimeMillis()
        return ScheduleMatch(
            id = id,
            time = timeFmt.format(Date(startTimeMs)),
            title = title,
            competition = competition.name,
            sport = when (competition.id) {
                "football"   -> SportFilter.FOOTBALL
                "hockey"     -> SportFilter.HOCKEY
                "basketball" -> SportFilter.BASKETBALL
                "volleyball" -> SportFilter.VOLLEYBALL
                "handball"   -> SportFilter.HANDBALL
                else         -> SportFilter.ALL
            },
            status = when {
                isLive            -> MatchStatus.LIVE
                startTimeMs < now -> MatchStatus.FINISHED
                else              -> MatchStatus.UPCOMING
            },
            isSubscriptionRequired = isSubscriptionRequired
        )
    }

    private companion object {
        val WEEK_SHORT = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        val WEEK_FULL = listOf(
            "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"
        )
    }
}
