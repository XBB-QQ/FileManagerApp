package com.filemanager.app

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for FileManagerApp.
 * Bootstraps Hilt dependency injection.
 */
@HiltAndroidApp
class FileManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("FileManagerApp", "Application.onCreate()")
    }
}
