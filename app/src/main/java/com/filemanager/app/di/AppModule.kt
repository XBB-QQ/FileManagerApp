package com.filemanager.app.di

import android.content.Context
import com.filemanager.app.data.local.FileManagerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-level dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideFileManagerDatabase(@ApplicationContext context: Context): FileManagerDatabase {
        return FileManagerDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideRecentFileDao(database: FileManagerDatabase) = database.recentFileDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(database: FileManagerDatabase) = database.favoriteDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: FileManagerDatabase) = database.searchHistoryDao()
}
