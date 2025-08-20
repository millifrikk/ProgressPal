package com.progresspal.app.data.repository

import androidx.lifecycle.LiveData
import com.progresspal.app.data.database.dao.WeightDao
import com.progresspal.app.data.database.entities.WeightEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class WeightRepository(private val weightDao: WeightDao) {
    
    fun getAllWeightsLiveData(userId: Long): LiveData<List<WeightEntity>> = 
        weightDao.getAllWeights(userId)
    
    fun getRecentWeights(userId: Long, limit: Int = 30): LiveData<List<WeightEntity>> = 
        weightDao.getRecentWeights(userId, limit)
    
    fun getLatestWeight(userId: Long): LiveData<WeightEntity?> = 
        weightDao.getLatestWeight(userId)
    
    suspend fun getLatestWeightSync(userId: Long): WeightEntity? = withContext(Dispatchers.IO) {
        weightDao.getLatestWeightSync(userId)
    }
    
    suspend fun getAllWeightsSync(userId: Long): List<WeightEntity> = withContext(Dispatchers.IO) {
        weightDao.getAllWeightsSync(userId)
    }
    
    suspend fun getAllWeights(userId: Long): List<WeightEntity> = withContext(Dispatchers.IO) {
        weightDao.getAllWeightsSync(userId)
    }
    
    fun getWeightsInRange(userId: Long, startDate: Date, endDate: Date): LiveData<List<WeightEntity>> = 
        weightDao.getWeightsInRange(userId, startDate, endDate)
    
    suspend fun getWeightById(weightId: Long): WeightEntity? = withContext(Dispatchers.IO) {
        weightDao.getWeightById(weightId)
    }
    
    suspend fun insertWeight(weight: WeightEntity): Long = withContext(Dispatchers.IO) {
        weightDao.insertWeight(weight)
    }
    
    suspend fun updateWeight(weight: WeightEntity) = withContext(Dispatchers.IO) {
        weightDao.updateWeight(weight)
    }
    
    suspend fun deleteWeight(weight: WeightEntity) = withContext(Dispatchers.IO) {
        weightDao.deleteWeight(weight)
    }
    
    suspend fun deleteWeightById(weightId: Long) = withContext(Dispatchers.IO) {
        weightDao.deleteWeightById(weightId)
    }
    
    suspend fun deleteAllWeights(userId: Long): Unit = withContext(Dispatchers.IO) {
        weightDao.deleteAllWeights(userId)
    }
    
    suspend fun getWeightCount(userId: Long): Int = withContext(Dispatchers.IO) {
        weightDao.getWeightCount(userId)
    }
    
    suspend fun getAverageWeightInPeriod(userId: Long, startDate: Date, endDate: Date): Float? = 
        withContext(Dispatchers.IO) {
            weightDao.getAverageWeightInPeriod(userId, startDate, endDate)
        }
}