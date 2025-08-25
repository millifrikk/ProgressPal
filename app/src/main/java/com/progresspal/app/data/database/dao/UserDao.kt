package com.progresspal.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.progresspal.app.data.database.entities.UserEntity

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): LiveData<UserEntity?>
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUserSync(): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdSync(userId: Long): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET current_weight = :weight WHERE id = :userId")
    suspend fun updateCurrentWeight(userId: Long, weight: Float)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}