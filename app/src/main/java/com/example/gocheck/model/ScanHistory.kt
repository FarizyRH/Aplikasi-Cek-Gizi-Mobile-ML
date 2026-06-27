package com.example.gocheck.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val foodName: String,
    val energy: Float,
    val protein: Float,
    val carbohydrates: Float,
    val fat: Float,
    val sugar: Float,
    val sodium: Float,
    val fiber: Float,
    val clusterIndex: Int,
    val confidence: Float = 100f,
    val timestamp: Long,
    val description: String
) : Parcelable
