package com.progresspal.app.data.repository

import androidx.lifecycle.LiveData
import com.progresspal.app.data.database.dao.MeasurementDao
import com.progresspal.app.data.database.entities.MeasurementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class MeasurementRepository(private val measurementDao: MeasurementDao) {
    
    fun getAllMeasurements(userId: Long): LiveData<List<MeasurementEntity>> = 
        measurementDao.getAllMeasurements(userId)
    
    fun getMeasurementsByType(userId: Long, type: String): LiveData<List<MeasurementEntity>> = 
        measurementDao.getMeasurementsByType(userId, type)
    
    suspend fun getLatestMeasurementByType(userId: Long, type: String): MeasurementEntity? = 
        withContext(Dispatchers.IO) {
            measurementDao.getLatestMeasurementByType(userId, type)
        }
    
    fun getMeasurementsByDate(userId: Long, date: Date): LiveData<List<MeasurementEntity>> = 
        measurementDao.getMeasurementsByDate(userId, date)
    
    fun getMeasurementsInRange(userId: Long, startDate: Date, endDate: Date): LiveData<List<MeasurementEntity>> = 
        measurementDao.getMeasurementsInRange(userId, startDate, endDate)
    
    fun getMeasurementTypes(userId: Long): LiveData<List<String>> = 
        measurementDao.getMeasurementTypes(userId)
    
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long = withContext(Dispatchers.IO) {
        measurementDao.insertMeasurement(measurement)
    }
    
    suspend fun insertMeasurements(measurements: List<MeasurementEntity>) = withContext(Dispatchers.IO) {
        measurementDao.insertMeasurements(measurements)
    }
    
    suspend fun updateMeasurement(measurement: MeasurementEntity) = withContext(Dispatchers.IO) {
        measurementDao.updateMeasurement(measurement)
    }
    
    suspend fun deleteMeasurement(measurement: MeasurementEntity) = withContext(Dispatchers.IO) {
        measurementDao.deleteMeasurement(measurement)
    }
    
    suspend fun deleteMeasurementById(measurementId: Long) = withContext(Dispatchers.IO) {
        measurementDao.deleteMeasurementById(measurementId)
    }
    
    suspend fun deleteMeasurementsByDate(userId: Long, date: Date) = withContext(Dispatchers.IO) {
        measurementDao.deleteMeasurementsByDate(userId, date)
    }
    
    suspend fun getMeasurementCount(userId: Long): Int = withContext(Dispatchers.IO) {
        measurementDao.getMeasurementCount(userId)
    }
}