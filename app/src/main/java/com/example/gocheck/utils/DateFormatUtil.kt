package com.example.gocheck.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Utility untuk formatting tanggal dan waktu relatif
 */
object DateFormatUtil {

    /**
     * Mendapatkan waktu relatif dari timestamp
     * Contoh: "Baru saja", "5 menit lalu", "2 jam lalu", "3 hari lalu"
     *
     * @param timestamp Timestamp dalam milliseconds
     * @return String waktu relatif dalam Bahasa Indonesia
     */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        // Jika negatif (timestamp di masa depan), return "Baru saja"
        if (diff < 0) return "Baru saja"

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            seconds < 60 -> "Baru saja"
            minutes < 2 -> "1 menit lalu"
            minutes < 60 -> "$minutes menit lalu"
            hours < 2 -> "1 jam lalu"
            hours < 24 -> "$hours jam lalu"
            days < 2 -> "Kemarin"
            days < 7 -> "$days hari lalu"
            days < 14 -> "1 minggu lalu"
            days < 30 -> "${days / 7} minggu lalu"
            days < 60 -> "1 bulan lalu"
            days < 365 -> "${days / 30} bulan lalu"
            else -> {
                val years = days / 365
                if (years < 2) "1 tahun lalu" else "$years tahun lalu"
            }
        }
    }

    /**
     * Format timestamp ke string tanggal lengkap
     * Format: "15 Desember 2025, 14:30"
     *
     * @param timestamp Timestamp dalam milliseconds
     * @return String tanggal terformat
     */
    fun formatFullDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
        return format.format(date)
    }

    /**
     * Format timestamp ke string tanggal pendek
     * Format: "15 Des 2025"
     *
     * @param timestamp Timestamp dalam milliseconds
     * @return String tanggal terformat pendek
     */
    fun formatShortDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return format.format(date)
    }

    /**
     * Format timestamp ke string waktu saja
     * Format: "14:30"
     *
     * @param timestamp Timestamp dalam milliseconds
     * @return String waktu terformat
     */
    fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        return format.format(date)
    }

    /**
     * Mengecek apakah timestamp adalah hari ini
     *
     * @param timestamp Timestamp dalam milliseconds
     * @return True jika timestamp adalah hari ini
     */
    fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        val timestampDay = calendar.get(Calendar.DAY_OF_YEAR)
        val timestampYear = calendar.get(Calendar.YEAR)

        return todayDay == timestampDay && todayYear == timestampYear
    }

    /**
     * Mengecek apakah timestamp adalah kemarin
     *
     * @param timestamp Timestamp dalam milliseconds
     * @return True jika timestamp adalah kemarin
     */
    fun isYesterday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDay = calendar.get(Calendar.DAY_OF_YEAR)
        val yesterdayYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        val timestampDay = calendar.get(Calendar.DAY_OF_YEAR)
        val timestampYear = calendar.get(Calendar.YEAR)

        return yesterdayDay == timestampDay && yesterdayYear == timestampYear
    }

    /**
     * Format timestamp dengan konteks (hari ini, kemarin, atau tanggal lengkap)
     * Format:
     * - Hari ini: "Hari ini, 14:30"
     * - Kemarin: "Kemarin, 14:30"
     * - Lainnya: "15 Des 2025, 14:30"
     *
     * @param timestamp Timestamp dalam milliseconds
     * @return String dengan konteks waktu
     */
    fun formatWithContext(timestamp: Long): String {
        return when {
            isToday(timestamp) -> "Hari ini, ${formatTime(timestamp)}"
            isYesterday(timestamp) -> "Kemarin, ${formatTime(timestamp)}"
            else -> {
                val date = Date(timestamp)
                val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                format.format(date)
            }
        }
    }

    /**
     * Mendapatkan greeting berdasarkan waktu
     *
     * @return "Selamat Pagi", "Selamat Siang", "Selamat Sore", atau "Selamat Malam"
     */
    fun getGreeting(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }
}