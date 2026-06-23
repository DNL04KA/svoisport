package com.svoysport.tv.session

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * Избранные трансляции. Хранит набор id матчей в SharedPreferences и держит
 * реактивную копию в Compose-state, чтобы карточки/экраны перерисовывались
 * при добавлении/удалении из избранного.
 *
 * Инициализируется один раз в [com.svoysport.tv.SvoySportApp.onCreate].
 * Доступ из любого composable — как у [SessionManager].
 */
object FavoritesManager {

    private const val PREFS = "favorites"
    private const val KEY_IDS = "match_ids"

    private var prefs: android.content.SharedPreferences? = null

    /** Реактивный набор id для наблюдения из Compose. */
    val favoriteIds = mutableStateOf<Set<String>>(emptySet())

    fun init(context: Context) {
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        favoriteIds.value = p.getStringSet(KEY_IDS, emptySet())?.toSet() ?: emptySet()
    }

    fun isFavorite(matchId: String): Boolean = matchId in favoriteIds.value

    fun toggle(matchId: String) {
        val updated = favoriteIds.value.toMutableSet().apply {
            if (!add(matchId)) remove(matchId)
        }
        favoriteIds.value = updated
        prefs?.edit()?.putStringSet(KEY_IDS, updated)?.apply()
    }
}
