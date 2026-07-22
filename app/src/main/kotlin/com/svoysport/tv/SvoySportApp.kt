package com.svoysport.tv

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.allowHardware
import com.svoysport.tv.session.FavoritesManager
import com.svoysport.tv.session.SubscriptionManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SvoySportApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        FavoritesManager.init(this)
        SubscriptionManager.init(this)
    }

    /**
     * Coil 3 (alpha) не всегда подхватывает сетевой загрузчик через ServiceLoader,
     * поэтому регистрируем OkHttp-фетчер явно — иначе сетевые картинки не грузятся.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .allowHardware(false)   // часть TV-устройств не декодируют hardware bitmaps
            .build()
}
