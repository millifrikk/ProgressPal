package com.progresspal.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.progresspal.app.data.database.converters.DateConverters
import com.progresspal.app.data.database.dao.*
import com.progresspal.app.data.database.entities.*

@Database(
    entities = [
        UserEntity::class,
        WeightEntity::class,
        MeasurementEntity::class,
        GoalEntity::class,
        PhotoEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class ProgressPalDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun weightDao(): WeightDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun goalDao(): GoalDao
    abstract fun photoDao(): PhotoDao
    
    companion object {
        @Volatile
        private var INSTANCE: ProgressPalDatabase? = null
        
        fun getDatabase(context: Context): ProgressPalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProgressPalDatabase::class.java,
                    "progresspal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}