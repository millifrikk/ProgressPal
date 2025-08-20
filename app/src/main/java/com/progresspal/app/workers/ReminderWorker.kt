package com.progresspal.app.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.progresspal.app.utils.NotificationHelper

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    
    override fun doWork(): Result {
        return try {
            // Show daily reminder notification
            NotificationHelper.showDailyReminder(applicationContext)
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }
    
    companion object {
        const val WORK_NAME = "daily_weight_reminder"
    }
}