package com.example.gocheck.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gocheck.model.ScanHistory

@Database(entities = [ScanHistory::class], version = 1, exportSchema = false)
abstract class CekGiziDatabase : RoomDatabase() {

    abstract fun scanHistoryDao(): ScanHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: CekGiziDatabase? = null

        fun getDatabase(context: Context): CekGiziDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CekGiziDatabase::class.java,
                    "cek_gizi_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
