package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HealthLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthLogDao {
    @Query("SELECT * FROM health_logs ORDER BY missionDay ASC, timestamp ASC")
    fun getAllLogs(): Flow<List<HealthLog>>

    @Query("SELECT * FROM health_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLog(): Flow<HealthLog?>

    @Query("SELECT * FROM health_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<HealthLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HealthLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<HealthLog>)

    @Query("DELETE FROM health_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM health_logs")
    suspend fun clearAll()
}
