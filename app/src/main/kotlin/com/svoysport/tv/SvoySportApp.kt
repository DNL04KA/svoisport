package com.svoysport.tv

import android.app.Application
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.allowHardware
import com.svoysport.tv.session.FavoritesManager
import com.svoysport.tv.session.SubscriptionManager
import com.svoysport.tv.session.SettingsManager
import com.svoysport.tv.reminder.MatchReminderManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SvoySportApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        FavoritesManager.init(this)
        SubscriptionManager.init(this)
        SettingsManager.init(this)
        MatchReminderManager.init(this)
    }

    /**
     * Coil 3 (alpha) не всегда подхватывает сетевой загрузчик через ServiceLoader,
     * поэтому регистрируем OkHttp-фетчер явно — иначе сетевые картинки не грузятся.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .allowHardware(shouldAllowHardwareBitmaps(Build.VERSION.SDK_INT))
            .build()
}

/** Hardware textures avoid a CPU bitmap upload on every image draw on modern TVs. */
internal fun shouldAllowHardwareBitmaps(sdkInt: Int): Boolean = sdkInt >= Build.VERSION_CODES.O
