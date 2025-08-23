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
        PhotoEntity::class,
        BloodPressureEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class ProgressPalDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun weightDao(): WeightDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun goalDao(): GoalDao
    abstract fun photoDao(): PhotoDao
    abstract fun bloodPressureDao(): BloodPressureDao
    
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
        
        // Migration from version 2 to 3: Add blood pressure tracking table
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create blood pressure table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS blood_pressure (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        systolic INTEGER NOT NULL,
                        diastolic INTEGER NOT NULL,
                        pulse INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        timeOfDay TEXT NOT NULL,
                        tags TEXT,
                        notes TEXT,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                
                // Create indexes for blood pressure table (matching entity definition)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_bp_user_time ON blood_pressure(userId, timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_bp_user ON blood_pressure(userId)")
            }
        }
        
        // Migration from version 3 to 4: Fix blood pressure userId type mismatch
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop existing blood_pressure table if it exists (may have wrong schema)
                database.execSQL("DROP TABLE IF EXISTS blood_pressure")
                
                // Recreate blood pressure table with correct Long userId type
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS blood_pressure (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId INTEGER NOT NULL,
                        systolic INTEGER NOT NULL,
                        diastolic INTEGER NOT NULL,
                        pulse INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        timeOfDay TEXT NOT NULL,
                        tags TEXT,
                        notes TEXT,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                
                // Create indexes for blood pressure table (matching entity definition)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_bp_user_time ON blood_pressure(userId, timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_bp_user ON blood_pressure(userId)")
            }
        }
        
        // Migration from version 4 to 5: Add health settings fields to users table
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new health settings columns to users table
                database.execSQL("ALTER TABLE users ADD COLUMN birth_date INTEGER")
                database.execSQL("ALTER TABLE users ADD COLUMN waist_circumference REAL")
                database.execSQL("ALTER TABLE users ADD COLUMN hip_circumference REAL")
                database.execSQL("ALTER TABLE users ADD COLUMN measurement_system TEXT NOT NULL DEFAULT 'METRIC'")
                database.execSQL("ALTER TABLE users ADD COLUMN medical_guidelines TEXT NOT NULL DEFAULT 'US_AHA'")
                database.execSQL("ALTER TABLE users ADD COLUMN preferred_language TEXT NOT NULL DEFAULT 'en'")
            }
        }
        
        fun getDatabase(context: Context): ProgressPalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProgressPalDatabase::class.java,
                    "progresspal_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()  // For development: clear DB if migration fails
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}