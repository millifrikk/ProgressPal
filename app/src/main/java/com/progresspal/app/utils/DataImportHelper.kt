package com.progresspal.app.utils

import android.content.Context
import android.net.Uri
import com.progresspal.app.data.database.entities.WeightEntity
import com.progresspal.app.data.repository.WeightRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class DataImportHelper(
    private val context: Context,
    private val weightRepository: WeightRepository
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    suspend fun importWeightDataFromCsv(uri: Uri, userId: Long): ImportResult {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                    ?: return@withContext ImportResult.Error("Could not open file")
                
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readLines()
                reader.close()
                
                if (lines.isEmpty()) {
                    return@withContext ImportResult.Error("File is empty")
                }
                
                val weightEntries = mutableListOf<WeightEntity>()
                var successCount = 0
                var errorCount = 0
                
                // Skip header if present (look for common header patterns)
                val startIndex = if (lines.first().lowercase().contains("date") || 
                                     lines.first().lowercase().contains("weight")) 1 else 0
                
                for (i in startIndex until lines.size) {
                    val line = lines[i].trim()
                    if (line.isEmpty()) continue
                    
                    try {
                        val weightEntry = parseCsvLine(line, userId)
                        if (weightEntry != null) {
                            weightEntries.add(weightEntry)
                            successCount++
                        } else {
                            errorCount++
                        }
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
                
                if (weightEntries.isNotEmpty()) {
                    // Insert all valid entries
                    weightEntries.forEach { entry ->
                        weightRepository.insertWeight(entry)
                    }
                }
                
                ImportResult.Success(successCount, errorCount)
                
            } catch (e: Exception) {
                ImportResult.Error("Import failed: ${e.message}")
            }
        }
    }
    
    private fun parseCsvLine(line: String, userId: Long): WeightEntity? {
        try {
            val parts = line.split(",").map { it.trim().replace("\"", "") }
            
            if (parts.size < 2) return null
            
            // Try different CSV formats
            return when {
                parts.size >= 4 -> {
                    // Format: Date,Time,Weight (kg),Notes
                    val date = dateFormat.parse(parts[0]) ?: return null
                    val time = parts[1].takeIf { it.isNotEmpty() }
                    val weight = parts[2].toFloatOrNull() ?: return null
                    val notes = parts.getOrNull(3)?.takeIf { it.isNotEmpty() }
                    
                    WeightEntity(
                        userId = userId,
                        weight = weight,
                        date = date,
                        time = time,
                        notes = notes,
                        createdAt = Date()
                    )
                }
                parts.size >= 2 -> {
                    // Simple format: Date,Weight
                    val date = dateFormat.parse(parts[0]) ?: return null
                    val weight = parts[1].toFloatOrNull() ?: return null
                    
                    WeightEntity(
                        userId = userId,
                        weight = weight,
                        date = date,
                        createdAt = Date()
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            return null
        }
    }
    
    sealed class ImportResult {
        data class Success(val successCount: Int, val errorCount: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}