package com.example.gocheck.repository

import android.app.Application
import com.example.gocheck.database.CekGiziDatabase
import com.example.gocheck.database.ScanHistoryDao
import com.example.gocheck.model.ScanHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScanRepository(application: Application) {

    private val scanHistoryDao: ScanHistoryDao =
        CekGiziDatabase.getDatabase(application).scanHistoryDao()

    // READ - Get all scans
    fun getAllScans(): Flow<List<ScanHistory>> = scanHistoryDao.getAllScans()

    // READ - Get scan by ID
    suspend fun getScanById(scanId: Int): ScanHistory? = withContext(Dispatchers.IO) {
        scanHistoryDao.getScanById(scanId)
    }

    // READ - Get last scan
    suspend fun getLastScan(): ScanHistory? = withContext(Dispatchers.IO) {
        scanHistoryDao.getLastScan()
    }
    fun getScansToday(startOfDay: Long, endOfDay: Long): Flow<List<ScanHistory>> {
        return scanHistoryDao.getScansByDateRange(startOfDay, endOfDay)
    }
    // CREATE - Insert new scan
    suspend fun insertScan(scan: ScanHistory) = withContext(Dispatchers.IO) {
        scanHistoryDao.insertScan(scan)
    }

    // UPDATE - Update scan
    suspend fun updateScan(scan: ScanHistory) = withContext(Dispatchers.IO) {
        scanHistoryDao.updateScan(scan)
    }

    // DELETE - Delete specific scan
    suspend fun deleteScan(scan: ScanHistory) = withContext(Dispatchers.IO) {
        scanHistoryDao.deleteScan(scan)
    }

    // DELETE - Delete by ID
    suspend fun deleteScanById(scanId: Int) = withContext(Dispatchers.IO) {
        scanHistoryDao.deleteScanById(scanId)
    }

    // DELETE - Delete all
    suspend fun deleteAllScans() = withContext(Dispatchers.IO) {
        scanHistoryDao.deleteAllScans()
    }
}
