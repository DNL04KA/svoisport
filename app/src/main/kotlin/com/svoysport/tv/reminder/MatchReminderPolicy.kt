package com.svoysport.tv.reminder

import com.svoysport.tv.domain.model.MatchItem

internal const val REMINDER_LEAD_TIME_MS = 5 * 60_000L
internal const val REMINDER_ALERT_DURATION_MS = 10_000L

data class ReminderAlert(
    val matchId: String,
    val title: String,
    val category: String,
    val createdAtMs: Long
)

internal fun reminderAlert(
    matchId: String,
    title: String,
    category: String,
    createdAtMs: Long = System.currentTimeMillis()
) = ReminderAlert(matchId, title, category, createdAtMs)

internal fun canScheduleMatchReminder(
    match: MatchItem,
    isLoggedIn: Boolean,
    isSubscribed: Boolean,
    nowMs: Long
): Boolean =
    isLoggedIn &&
        !match.isLive &&
        match.durationSec == 0L &&
        match.startTimeMs - nowMs > REMINDER_LEAD_TIME_MS &&
        (!match.isSubscriptionRequired || isSubscribed)

internal fun reminderTriggerAt(startTimeMs: Long): Long =
    startTimeMs - REMINDER_LEAD_TIME_MS
