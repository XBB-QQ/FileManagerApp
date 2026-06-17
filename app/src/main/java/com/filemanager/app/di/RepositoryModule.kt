package com.filemanager.app.di

import com.filemanager.app.data.repository.FileRepositoryImpl
import com.filemanager.app.data.repository.RecentFileRepositoryImpl
import com.filemanager.app.domain.repository.FileRepository
import com.filemanager.app.domain.repository.RecentFileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindRecentFileRepository(impl: RecentFileRepositoryImpl): RecentFileRepository
}
