package com.progresspal.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.progresspal.app.data.database.entities.GoalEntity

@Dao
interface GoalDao {
    
    @Query("SELECT * FROM goals WHERE user_id = :userId ORDER BY created_at DESC")
    fun getAllGoals(userId: Long): LiveData<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE user_id = :userId AND achieved = 0 ORDER BY created_at DESC")
    fun getActiveGoals(userId: Long): LiveData<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE user_id = :userId AND achieved = 1 ORDER BY achieved_date DESC")
    fun getAchievedGoals(userId: Long): LiveData<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE user_id = :userId AND goal_type = :type ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestGoalByType(userId: Long, type: String): GoalEntity?
    
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): GoalEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long
    
    @Update
    suspend fun updateGoal(goal: GoalEntity)
    
    @Query("UPDATE goals SET achieved = 1, achieved_date = :achievedDate WHERE id = :goalId")
    suspend fun markGoalAsAchieved(goalId: Long, achievedDate: java.util.Date)
    
    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
    
    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)
    
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId AND achieved = 0")
    suspend fun getActiveGoalCount(userId: Long): Int
}