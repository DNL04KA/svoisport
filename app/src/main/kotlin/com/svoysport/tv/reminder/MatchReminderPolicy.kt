package com.svoysport.tv.reminder

import com.svoysport.tv.domain.model.MatchItem

internal const val REMINDER_LEAD_TIME_MS = 5 * 60_000L

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
