package com.progresspal.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    private val displayDateFormat = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat(Constants.DATE_FORMAT_SHORT, Locale.getDefault())
    private val timeFormat = SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault())
    
    fun formatDisplayDate(date: Date): String {
        return displayDateFormat.format(date)
    }
    
    fun formatShortDate(date: Date): String {
        return shortDateFormat.format(date)
    }
    
    fun formatTime(date: Date): String {
        return timeFormat.format(date)
    }
    
    fun formatDisplayTime(date: Date): String {
        return timeFormat.format(date)
    }
    
    fun formatDateTime(date: Date): String {
        return dateTimeFormat.format(date)
    }
    
    fun getRelativeDate(date: Date): String {
        val now = Date()
        val diffInMillis = now.time - date.time
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        
        return when {
            diffInDays == 0 -> "Today"
            diffInDays == 1 -> "Yesterday"
            diffInDays < 7 -> "$diffInDays days ago"
            diffInDays < 30 -> "${diffInDays / 7} week${if (diffInDays / 7 > 1) "s" else ""} ago"
            diffInDays < 365 -> "${diffInDays / 30} month${if (diffInDays / 30 > 1) "s" else ""} ago"
            else -> "${diffInDays / 365} year${if (diffInDays / 365 > 1) "s" else ""} ago"
        }
    }
    
    fun getStartOfDay(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
    
    fun getEndOfDay(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }
    
    fun getWeekStart(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return getStartOfDay(calendar.time)
    }
    
    fun getWeekEnd(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = getWeekStart(date)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        return getEndOfDay(calendar.time)
    }
    
    fun getMonthStart(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return getStartOfDay(calendar.time)
    }
    
    fun getMonthEnd(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return getEndOfDay(calendar.time)
    }
    
    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }
    
    fun addWeeks(date: Date, weeks: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.WEEK_OF_YEAR, weeks)
        return calendar.time
    }
    
    fun isToday(date: Date): Boolean {
        val today = getStartOfDay()
        val tomorrow = addDays(today, 1)
        return date.time >= today.time && date.time < tomorrow.time
    }
    
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}