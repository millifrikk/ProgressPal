package com.progresspal.app.utils

object Constants {
    
    // Database
    const val DATABASE_NAME = "progresspal_database"
    const val DATABASE_VERSION = 2
    
    // Shared Preferences
    const val PREFS_NAME = "progresspal_prefs"
    const val PREF_FIRST_LAUNCH = "first_launch"
    const val PREF_USER_ID = "user_id"
    const val PREF_REMINDER_TIME = "reminder_time"
    const val PREF_REMINDER_ENABLED = "reminder_enabled"
    
    // Date Formats
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val DATE_FORMAT_SHORT = "dd/MM/yy"
    const val TIME_FORMAT = "HH:mm"
    const val DATE_TIME_FORMAT = "dd MMM yyyy, HH:mm"
    
    // Measurement Types
    val MEASUREMENT_TYPES = listOf(
        "waist",
        "chest", 
        "hips",
        "neck",
        "biceps_left",
        "biceps_right",
        "thigh_left",
        "thigh_right"
    )
    
    // Goal Types
    val GOAL_TYPES = listOf(
        "weight",
        "waist",
        "chest",
        "hips"
    )
    
    // Activity Levels
    val ACTIVITY_LEVELS = listOf(
        "sedentary",
        "lightly_active",
        "moderately_active",
        "very_active",
        "extremely_active"
    )
    
    // Limits
    const val MAX_WEIGHT_KG = 500f
    const val MIN_WEIGHT_KG = 20f
    const val MAX_HEIGHT_CM = 250f
    const val MIN_HEIGHT_CM = 100f
    const val MAX_MEASUREMENT_CM = 200f
    const val MIN_MEASUREMENT_CM = 10f
    
    // Chart
    const val CHART_ANIMATION_DURATION = 1000
    const val MAX_CHART_ENTRIES = 100
    
    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "progresspal_reminders"
    const val NOTIFICATION_CHANNEL_NAME = "Daily Reminders"
    const val NOTIFICATION_ID_REMINDER = 1001
    
    // Request Codes
    const val REQUEST_CAMERA_PERMISSION = 1001
    const val REQUEST_STORAGE_PERMISSION = 1002
    const val REQUEST_PICK_IMAGE = 2001
    const val REQUEST_CAPTURE_IMAGE = 2002
}