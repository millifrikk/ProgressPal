package com.progresspal.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.progresspal.app.R
import com.progresspal.app.MainActivity

object NotificationHelper {
    
    // Notification Channel IDs
    private const val REMINDER_CHANNEL_ID = "weight_reminder_channel"
    private const val MILESTONE_CHANNEL_ID = "milestone_channel"
    private const val WEEKLY_SUMMARY_CHANNEL_ID = "weekly_summary_channel"
    private const val GOAL_ACHIEVEMENT_CHANNEL_ID = "goal_achievement_channel"
    
    // Notification IDs
    const val DAILY_REMINDER_ID = 1001
    const val MILESTONE_ID = 1002
    const val WEEKLY_SUMMARY_ID = 1003
    const val GOAL_ACHIEVEMENT_ID = 1004
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Daily Reminder Channel
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Daily Weight Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to log your weight"
                enableVibration(true)
            }
            
            // Milestone Achievement Channel
            val milestoneChannel = NotificationChannel(
                MILESTONE_CHANNEL_ID,
                "Milestone Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for reaching milestones"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Weekly Summary Channel
            val weeklySummaryChannel = NotificationChannel(
                WEEKLY_SUMMARY_CHANNEL_ID,
                "Weekly Summaries",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly progress summaries"
            }
            
            // Goal Achievement Channel
            val goalAchievementChannel = NotificationChannel(
                GOAL_ACHIEVEMENT_CHANNEL_ID,
                "Goal Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when you reach your goals"
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannels(listOf(
                reminderChannel,
                milestoneChannel,
                weeklySummaryChannel,
                goalAchievementChannel
            ))
        }
    }
    
    fun showDailyReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle("Time to log your weight!")
            .setContentText("Don't forget to track your progress today 📊")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        NotificationManagerCompat.from(context).notify(DAILY_REMINDER_ID, notification)
    }
    
    fun showMilestoneAchievement(context: Context, milestoneMessage: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "statistics")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, MILESTONE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_statistics)
            .setContentTitle("Milestone Achieved! 🎉")
            .setContentText(milestoneMessage)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(milestoneMessage))
            .build()
        
        NotificationManagerCompat.from(context).notify(MILESTONE_ID, notification)
    }
    
    fun showWeeklySummary(context: Context, summaryText: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "statistics")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, WEEKLY_SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_statistics)
            .setContentTitle("Your Weekly Progress")
            .setContentText(summaryText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summaryText))
            .build()
        
        NotificationManagerCompat.from(context).notify(WEEKLY_SUMMARY_ID, notification)
    }
    
    fun showGoalAchievement(context: Context, goalMessage: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "dashboard")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, GOAL_ACHIEVEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle("Goal Achieved! 🏆")
            .setContentText(goalMessage)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(goalMessage))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        NotificationManagerCompat.from(context).notify(GOAL_ACHIEVEMENT_ID, notification)
    }
}