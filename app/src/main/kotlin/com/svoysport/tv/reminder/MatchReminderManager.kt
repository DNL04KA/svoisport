package com.svoysport.tv.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.svoysport.tv.MainActivity
import com.svoysport.tv.R
import com.svoysport.tv.domain.model.MatchItem

object MatchReminderManager {
    private const val PREFS = "match_reminders"
    private const val KEY_IDS = "match_ids"
    private const val CHANNEL_ID = "match_reminders"
    private const val EXTRA_MATCH_ID = "match_id"
    private const val EXTRA_TITLE = "title"

    private lateinit var appContext: Context
    private val prefs by lazy { appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    val reminderIds = mutableStateOf<Set<String>>(emptySet())

    fun init(context: Context) {
        appContext = context.applicationContext
        createNotificationChannel()
        val now = System.currentTimeMillis()
        val active = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_IDS, emptySet()).orEmpty()
            .filterTo(mutableSetOf()) { id ->
                appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getLong(startKey(id), 0L) > now
            }
        reminderIds.value = active
        persistIds(active)
    }

    fun isEnabled(matchId: String): Boolean = matchId in reminderIds.value

    fun enable(match: MatchItem) {
        val triggerAt = reminderTriggerAt(match.startTimeMs)
        if (triggerAt <= System.currentTimeMillis()) return
        val updated = reminderIds.value + match.id
        reminderIds.value = updated
        prefs.edit()
            .putStringSet(KEY_IDS, updated)
            .putLong(startKey(match.id), match.startTimeMs)
            .putString(titleKey(match.id), match.title)
            .apply()
        alarmManager().setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            reminderPendingIntent(match.id, match.title)
        )
    }

    fun disable(matchId: String) {
        alarmManager().cancel(reminderPendingIntent(matchId, ""))
        removeStored(matchId)
    }

    internal fun markDelivered(matchId: String) = removeStored(matchId)

    private fun removeStored(matchId: String) {
        val updated = reminderIds.value - matchId
        reminderIds.value = updated
        prefs.edit()
            .putStringSet(KEY_IDS, updated)
            .remove(startKey(matchId))
            .remove(titleKey(matchId))
            .apply()
    }

    private fun persistIds(ids: Set<String>) {
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_IDS, ids).apply()
    }

    private fun alarmManager() = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun reminderPendingIntent(matchId: String, title: String): PendingIntent {
        val intent = Intent(appContext, MatchReminderReceiver::class.java)
            .putExtra(EXTRA_MATCH_ID, matchId)
            .putExtra(EXTRA_TITLE, title)
        return PendingIntent.getBroadcast(
            appContext,
            matchId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о трансляциях",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления за 5 минут до начала трансляции"
                setSound(null, null)
            }
            appContext.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    internal fun showNotification(matchId: String, title: String) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                appContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val openApp = PendingIntent.getActivity(
            appContext,
            matchId.hashCode(),
            Intent(appContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("Трансляция скоро начнётся")
            .setContentText(title)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setSilent(true)
            .setTimeoutAfter(10_000L)
            .build()
        NotificationManagerCompat.from(appContext).notify(matchId.hashCode(), notification)
    }

    internal fun matchId(intent: Intent): String = intent.getStringExtra(EXTRA_MATCH_ID).orEmpty()
    internal fun title(intent: Intent): String = intent.getStringExtra(EXTRA_TITLE).orEmpty()
    private fun startKey(id: String) = "start_$id"
    private fun titleKey(id: String) = "title_$id"
}

class MatchReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        MatchReminderManager.init(context)
        val matchId = MatchReminderManager.matchId(intent)
        if (matchId.isBlank()) return
        MatchReminderManager.showNotification(matchId, MatchReminderManager.title(intent))
        MatchReminderManager.markDelivered(matchId)
    }
}
