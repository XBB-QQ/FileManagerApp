package com.filemanager.app.di

import com.filemanager.app.domain.repository.FavoriteRepository
import com.filemanager.app.domain.repository.SearchRepository
import com.filemanager.app.data.repository.FavoriteRepositoryImpl
import com.filemanager.app.data.repository.SearchRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding additional repository interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository
}
