package com.progresspal.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.progresspal.app.utils.NotificationHelper

class ProgressPalApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channels for the app
        NotificationHelper.createNotificationChannels(this)
        
        // Initialize WorkManager with default configuration
        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(this, Configuration.Builder().build())
        }
    }
}