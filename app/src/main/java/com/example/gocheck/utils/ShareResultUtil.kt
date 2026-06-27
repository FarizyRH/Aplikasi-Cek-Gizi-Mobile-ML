package com.example.gocheck.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.gocheck.model.ScanHistory   // sesuaikan package ScanHistory-mu
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareResultUtil {

    /**
     * Generate formatted text untuk share hasil scan nutrisi
     */
    fun generateShareText(
        scan: ScanHistory,
        clusterNames: Map<Int, String>,
        clusterDescriptions: Map<Int, String>,
        healthRecommendations: Map<Int, String>
    ): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val scanDate = dateFormat.format(Date(scan.timestamp))

        val clusterName = clusterNames[scan.clusterIndex] ?: "Unknown"
        val clusterDesc = clusterDescriptions[scan.clusterIndex] ?: ""
        val recommendation = healthRecommendations[scan.clusterIndex] ?: ""

        return buildString {
            append("📋 HASIL SCAN NUTRISI GoCheck\n")
            append("══════════════════════════════════════\n\n")

            // Product Info
            append("🥗 Produk: ${scan.foodName}\n")
            append("📅 Tanggal Scan: $scanDate\n")
            append("Confidence: ${scan.confidence.toInt()}%\n\n")

            // Cluster Result
            append("🎯 Kategori: $clusterName\n")
            append("Deskripsi: $clusterDesc\n\n")

            // Nutrition Details
            append("📊 RINCIAN NUTRISI:\n")
            append("├─ Energy: ${scan.energy} kcal\n")
            append("├─ Protein: ${scan.protein} g\n")
            append("├─ Carbohydrates: ${scan.carbohydrates} g\n")
            append("├─ Fat: ${scan.fat} g\n")
            append("├─ Sugar: ${scan.sugar} g\n")
            append("├─ Sodium: ${scan.sodium} mg\n")
            append("└─ Fiber: ${scan.fiber} g\n\n")

            // Summary
            val totalCalories = scan.energy
            val totalMacros = scan.protein + scan.carbohydrates + scan.fat
            append("📈 RINGKASAN:\n")
            append("Total Kalori: $totalCalories kcal\n")
            append("Total Makronutrisi: $totalMacros g\n\n")

            // Recommendation
            append("💡 REKOMENDASI KESEHATAN:\n")
            append("$recommendation\n\n")

            append("══════════════════════════════════════\n")
            append("Dibuat menggunakan GoCheck App\n")
            append("Aplikasi Analisis Nutrisi Kemasan")
        }
    }

    /**
     * Share hasil ke apps (WhatsApp, Email, SMS, etc)
     * Menggunakan Android Intent Chooser
     */
    fun shareViaApps(
        context: Context,
        text: String,
        title: String = "Bagikan Hasil"
    ) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val chooserIntent = Intent.createChooser(shareIntent, title)
        context.startActivity(chooserIntent)
    }

    /**
     * Copy hasil ke clipboard
     * User bisa paste ke apps lain (WhatsApp, Notes, Email, dll)
     */
    fun copyToClipboard(
        context: Context,
        text: String,
        onCopyComplete: (() -> Unit)? = null
    ) {
        val clipboardManager = ContextCompat.getSystemService(
            context,
            android.content.ClipboardManager::class.java
        )

        val clipData = android.content.ClipData.newPlainText("Hasil Scan Nutrisi", text)
        clipboardManager?.setPrimaryClip(clipData)

        onCopyComplete?.invoke()
    }

    /**
     * Format hasil scan untuk PDF export (future feature)
     * Bisa digunakan nanti untuk export ke file
     */
    fun generatePdfContent(
        scan: ScanHistory,
        clusterNames: Map<Int, String>
    ): String {
        return """
            Hasil Scan Nutrisi - GoCheck

            Produk: ${scan.foodName}
            Kategori: ${clusterNames[scan.clusterIndex]}
            Waktu: ${formatTimestamp(scan.timestamp)}

            Rincian Nutrisi
            Energy: ${scan.energy} kcal
            Protein: ${scan.protein} g
            Carbohydrates: ${scan.carbohydrates} g
            Fat: ${scan.fat} g
            Sugar: ${scan.sugar} g
            Sodium: ${scan.sodium} mg
            Fiber: ${scan.fiber} g
        """.trimIndent()
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale("id", "ID"))
        return dateFormat.format(Date(timestamp))
    }
}
