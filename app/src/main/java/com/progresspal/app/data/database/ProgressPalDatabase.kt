package com.progresspal.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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
        
        // Migration from version 1 to 2: Add indexes for foreign keys and performance optimization
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create indexes for photos table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_photos_user_id ON photos (user_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_photos_weight_id ON photos (weight_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_photos_date ON photos (date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_photos_user_id_date ON photos (user_id, date)")
                
                // Create indexes for weights table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_weights_user_id ON weights (user_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_weights_date ON weights (date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_weights_user_id_date ON weights (user_id, date)")
                
                // Create indexes for measurements table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_measurements_user_id ON measurements (user_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_measurements_measurement_type ON measurements (measurement_type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_measurements_date ON measurements (date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_measurements_user_id_measurement_type ON measurements (user_id, measurement_type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_measurements_user_id_date ON measurements (user_id, date)")
                
                // Create indexes for goals table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_goals_user_id ON goals (user_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_goals_goal_type ON goals (goal_type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_goals_achieved ON goals (achieved)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_goals_user_id_goal_type ON goals (user_id, goal_type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_goals_user_id_achieved ON goals (user_id, achieved)")
            }
        }
        
        fun getDatabase(context: Context): ProgressPalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProgressPalDatabase::class.java,
                    "progresspal_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}