package com.progresspal.app.utils

import android.content.Context
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DataBackupHelper(
    private val context: Context,
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    
    suspend fun createBackup(): BackupResult {
        return withContext(Dispatchers.IO) {
            try {
                val user = userRepository.getUserSync()
                val weights = if (user != null) {
                    weightRepository.getAllWeightsSync(user.id)
                } else {
                    emptyList()
                }
                
                val backupData = JSONObject().apply {
                    put("version", "1.0")
                    put("timestamp", dateFormat.format(Date()))
                    put("app", "ProgressPal")
                    
                    // User data
                    if (user != null) {
                        put("user", JSONObject().apply {
                            put("name", user.name)
                            put("age", user.age)
                            put("height", user.height)
                            put("gender", user.gender)
                            put("activityLevel", user.activityLevel)
                            put("initialWeight", user.initialWeight)
                            put("currentWeight", user.currentWeight)
                            put("targetWeight", user.targetWeight)
                            put("createdAt", dateFormat.format(user.createdAt))
                            put("updatedAt", dateFormat.format(user.updatedAt))
                        })
                    }
                    
                    // Weight entries
                    put("weights", JSONArray().apply {
                        weights.forEach { weight ->
                            put(JSONObject().apply {
                                put("weight", weight.weight)
                                put("date", dateFormat.format(weight.date))
                                put("time", weight.time)
                                put("notes", weight.notes)
                                put("createdAt", dateFormat.format(weight.createdAt))
                            })
                        }
                    })
                }
                
                val fileName = "progresspal_backup_${System.currentTimeMillis()}.json"
                val backupDir = File(context.getExternalFilesDir(null), "backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }
                
                val backupFile = File(backupDir, fileName)
                backupFile.writeText(backupData.toString(2))
                
                BackupResult.Success(backupFile)
                
            } catch (e: Exception) {
                BackupResult.Error("Backup failed: ${e.message}")
            }
        }
    }
    
    suspend fun restoreBackup(backupFile: File, userId: Long): RestoreResult {
        return withContext(Dispatchers.IO) {
            try {
                if (!backupFile.exists()) {
                    return@withContext RestoreResult.Error("Backup file not found")
                }
                
                val backupData = JSONObject(backupFile.readText())
                
                // Validate backup format
                if (!backupData.has("version") || !backupData.has("app")) {
                    return@withContext RestoreResult.Error("Invalid backup file format")
                }
                
                var restoredCount = 0
                
                // Restore weight entries
                if (backupData.has("weights")) {
                    val weightsArray = backupData.getJSONArray("weights")
                    for (i in 0 until weightsArray.length()) {
                        try {
                            val weightObj = weightsArray.getJSONObject(i)
                            val weightEntity = com.progresspal.app.data.database.entities.WeightEntity(
                                userId = userId,
                                weight = weightObj.getDouble("weight").toFloat(),
                                date = dateFormat.parse(weightObj.getString("date")) ?: Date(),
                                time = weightObj.optString("time", null),
                                notes = weightObj.optString("notes", null),
                                createdAt = if (weightObj.has("createdAt")) {
                                    dateFormat.parse(weightObj.getString("createdAt")) ?: Date()
                                } else Date()
                            )
                            weightRepository.insertWeight(weightEntity)
                            restoredCount++
                        } catch (e: Exception) {
                            // Skip invalid entries
                        }
                    }
                }
                
                RestoreResult.Success(restoredCount)
                
            } catch (e: Exception) {
                RestoreResult.Error("Restore failed: ${e.message}")
            }
        }
    }
    
    fun listBackups(): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), "backups")
        return if (backupDir.exists()) {
            backupDir.listFiles { file -> file.extension == "json" }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    sealed class BackupResult {
        data class Success(val file: File) : BackupResult()
        data class Error(val message: String) : BackupResult()
    }
    
    sealed class RestoreResult {
        data class Success(val restoredCount: Int) : RestoreResult()
        data class Error(val message: String) : RestoreResult()
    }
}