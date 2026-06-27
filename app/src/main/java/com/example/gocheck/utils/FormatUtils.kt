package com.example.gocheck.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    private val idLocale = Locale("id", "ID")

    // Format angka biasa (misal: 12.5)
    fun formatNumber(number: Float, decimals: Int = 1): String {
        return String.format(idLocale, "%.${decimals}f", number)
    }

    // BARU: Format angka dengan satuan (misal: "120 kcal", "5.2 g")
    // Ini menghilangkan duplikasi kode "%.1f g" di mana-mana
    fun formatNutrient(value: Float, unit: String, decimals: Int = 1): String {
        return "${formatNumber(value, decimals)} $unit"
    }

    // Format Tanggal Pendek (01/01/2026)
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", idLocale)
        return sdf.format(Date(timestamp))
    }

    // BARU: Format Tanggal Lengkap untuk Header (Senin, 1 Januari 2026)
    fun formatDateFull(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", idLocale)
        return sdf.format(Date(timestamp))
    }

    // BARU: Format Jam untuk 'Terakhir Discan' (14:30)
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", idLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatClusterName(index: Int): String {
        return when (index) {
            0 -> "Makanan Manis"
            1 -> "Makanan Padat"
            2 -> "Diet Sehat"
            3 -> "Kaya Serat"
            4 -> "Makanan Asin"
            else -> "Unknown"
        }
    }
}