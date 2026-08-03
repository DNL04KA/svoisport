package com.svoysport.tv.session

import android.content.Context

object SettingsManager {
    private const val PREFS = "tv_settings"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_QUALITY = "quality"
    private lateinit var context: Context

    fun init(appContext: Context) { context = appContext.applicationContext }
    fun language(): String = context.getSharedPreferences(PREFS, 0).getString(KEY_LANGUAGE, "Русский") ?: "Русский"
    fun quality(): String = context.getSharedPreferences(PREFS, 0).getString(KEY_QUALITY, "Авто") ?: "Авто"
    fun save(language: String, quality: String) {
        context.getSharedPreferences(PREFS, 0).edit().putString(KEY_LANGUAGE, language).putString(KEY_QUALITY, quality).apply()
    }
    fun maxVideoHeight(): Int? = quality().toIntOrNull()
}
