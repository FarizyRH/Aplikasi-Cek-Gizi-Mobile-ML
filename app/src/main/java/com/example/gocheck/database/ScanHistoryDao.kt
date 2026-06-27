package com.example.gocheck.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gocheck.model.ScanHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {

    @Query("SELECT * FROM scan_history WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getScansByDateRange(startDate: Long, endDate: Long): Flow<List<ScanHistory>>
    // CREATE
    @Insert
    suspend fun insertScan(scan: ScanHistory)

    // READ – list history
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistory>>

    // READ – by id (TANPA ORDER BY)
    @Query("SELECT * FROM scan_history WHERE id = :scanId LIMIT 1")
    suspend fun getScanById(scanId: Int): ScanHistory?

    // READ – last scan
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastScan(): ScanHistory?

    // UPDATE
    @Update
    suspend fun updateScan(scan: ScanHistory)

    // DELETE
    @Delete
    suspend fun deleteScan(scan: ScanHistory)

    @Query("DELETE FROM scan_history WHERE id = :scanId")
    suspend fun deleteScanById(scanId: Int)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAllScans()

    // COUNT
    @Query("SELECT COUNT(*) FROM scan_history")
    suspend fun getScansCount(): Int
}

