package com.filemanager.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database containing all local tables: recent files, favorites, search history.
 */
@Database(
    entities = [
        RecentFileEntity::class,
        FavoriteEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FileManagerDatabase : RoomDatabase() {
    abstract fun recentFileDao(): RecentFileDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: FileManagerDatabase? = null

        fun getInstance(context: Context): FileManagerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FileManagerDatabase::class.java,
                    "file_manager_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
