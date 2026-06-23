package com.svoysport.tv.di

import com.svoysport.tv.data.remote.SportTvMatchRepository
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
}
