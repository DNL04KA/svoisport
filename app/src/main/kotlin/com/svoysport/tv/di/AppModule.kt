package com.svoysport.tv.di

import com.svoysport.tv.data.remote.SportTvMatchRepository
import com.svoysport.tv.data.remote.activation.ActivationApi
import com.svoysport.tv.data.remote.activation.RealActivationApi
import com.svoysport.tv.domain.repository.MatchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Боевой источник — реальные трансляции с sport-tv.by.
     * Для офлайн-разработки можно временно вернуть MockMatchRepository.
     */
    @Binds
    @Singleton
    abstract fun bindMatchRepository(
        repository: SportTvMatchRepository
    ): MatchRepository

    /**
     * Активация подписки через общий сервер с мобильной QR-страницей.
     * Все сборки используют опубликованный Vercel API; адрес можно
     * переопределить Gradle-свойством activationApiBaseUrl.
     */
    @Binds
    @Singleton
    abstract fun bindActivationApi(
        impl: RealActivationApi
    ): ActivationApi
}
