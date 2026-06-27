package com.example.gocheck.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.gocheck.R

/**
 * Utility object untuk mengelola informasi cluster nutrisi
 * Menyediakan mapping warna, nama, deskripsi, dan rekomendasi kesehatan
 */
object ClusterHelper {

    // ===== Cluster Colors =====
    private val clusterColorResIds = mapOf(
        0 to R.color.cluster_danger_sugar,
        1 to R.color.cluster_neutral_dense,
        2 to R.color.cluster_healthy_diet,
        3 to R.color.cluster_special_fiber,
        4 to R.color.cluster_warning_salt
    )

    // ===== Cluster Names =====
    val clusterNames = mapOf(
        0 to "⚠️ Makanan Manis",
        1 to "⚡ Makanan Padat",
        2 to "✅ Diet Sehat",
        3 to "🌾 Kaya Serat",
        4 to "🧂 Makanan Asin"
    )

    // ===== Cluster Descriptions =====
    val clusterDescriptions = mapOf(
        0 to "Produk ini mengandung kadar gula yang tinggi. Kurangi konsumsi gula dalam diet harian Anda untuk kesehatan optimal.",
        1 to "Makanan ini padat kalori. Konsumsi dalam porsi terkontrol dan seimbang dengan aktivitas fisik Anda.",
        2 to "Pilihan yang baik untuk diet sehat! Produk ini memiliki nutrisi seimbang. Terus pertahankan pilihan yang sama.",
        3 to "Produk ini tinggi serat, sangat bagus untuk pencernaan dan kesehatan usus. Excellent choice!",
        4 to "Produk ini mengandung kadar garam (sodium) yang tinggi. Batasi asupan garam untuk menjaga tekanan darah."
    )

    // ===== Health Recommendations =====
    val healthRecommendations = mapOf(
        0 to "💡 Rekomendasi: Pilih makanan dengan kadar gula lebih rendah. Perhatikan label nutrisi saat berbelanja.",
        1 to "💡 Rekomendasi: Konsumsi dengan porsi kecil. Pastikan keseimbangan dengan sayuran dan buah-buahan.",
        2 to "💡 Rekomendasi: Terus konsumsi produk serupa. Produk ini cocok untuk program diet jangka panjang.",
        3 to "💡 Rekomendasi: Sangat baik untuk pencernaan. Combine dengan konsumsi air yang cukup setiap hari.",
        4 to "💡 Rekomendasi: Batasi frekuensi konsumsi. Cari alternatif produk dengan kadar garam lebih rendah."
    )

    // ===== Public Functions =====

    /**
     * Mendapatkan warna cluster berdasarkan index
     * @param context Context untuk resolving color resource
     * @param clusterIndex Index cluster (0-4)
     * @return Color integer
     */
    fun getClusterColor(context: Context, clusterIndex: Int): Int {
        val colorResId = clusterColorResIds[clusterIndex]
            ?: R.color.cluster_neutral_dense
        return ContextCompat.getColor(context, colorResId)
    }

    /**
     * Mendapatkan nama cluster dengan emoji
     * @param clusterIndex Index cluster (0-4)
     * @return Nama cluster dengan emoji
     */
    fun getClusterName(clusterIndex: Int): String {
        return clusterNames[clusterIndex] ?: "❓ Cluster Tidak Diketahui"
    }

    /**
     * Mendapatkan deskripsi cluster
     * @param clusterIndex Index cluster (0-4)
     * @return Deskripsi cluster
     */
    fun getClusterDescription(clusterIndex: Int): String {
        return clusterDescriptions[clusterIndex]
            ?: "Data cluster tidak tersedia. Konsultasikan dengan ahli gizi untuk informasi lebih lanjut."
    }

    /**
     * Mendapatkan rekomendasi kesehatan
     * @param clusterIndex Index cluster (0-4)
     * @return Rekomendasi kesehatan
     */
    fun getHealthRecommendation(clusterIndex: Int): String {
        return healthRecommendations[clusterIndex]
            ?: "💡 Rekomendasi: Konsumsi produk ini dengan bijak dan sesuai kebutuhan nutrisi Anda."
    }

    /**
     * Mendapatkan emoji cluster tanpa teks
     * @param clusterIndex Index cluster (0-4)
     * @return Emoji cluster
     */
    fun getClusterEmoji(clusterIndex: Int): String {
        return when (clusterIndex) {
            0 -> "⚠️"
            1 -> "⚡"
            2 -> "✅"
            3 -> "🌾"
            4 -> "🧂"
            else -> "❓"
        }
    }

    /**
     * Mengecek apakah cluster termasuk kategori healthy
     * @param clusterIndex Index cluster (0-4)
     * @return True jika healthy (cluster 2 atau 3)
     */
    fun isHealthyCluster(clusterIndex: Int): Boolean {
        return clusterIndex == 2 || clusterIndex == 3
    }

    /**
     * Mengecek apakah cluster termasuk kategori warning/danger
     * @param clusterIndex Index cluster (0-4)
     * @return True jika warning/danger (cluster 0, 4)
     */
    fun isDangerousCluster(clusterIndex: Int): Boolean {
        return clusterIndex == 0 || clusterIndex == 4
    }

    /**
     * Mendapatkan severity level cluster
     * @param clusterIndex Index cluster (0-4)
     * @return "high", "medium", atau "low"
     */
    fun getClusterSeverity(clusterIndex: Int): String {
        return when (clusterIndex) {
            0, 4 -> "high"      // Danger
            1 -> "medium"       // Neutral
            2, 3 -> "low"       // Healthy
            else -> "unknown"
        }
    }

    /**
     * Mendapatkan informasi lengkap cluster dalam satu object
     * @param context Context untuk resolving color
     * @param clusterIndex Index cluster (0-4)
     * @return ClusterInfo data class
     */
    fun getClusterInfo(context: Context, clusterIndex: Int): ClusterInfo {
        return ClusterInfo(
            index = clusterIndex,
            color = getClusterColor(context, clusterIndex),
            name = getClusterName(clusterIndex),
            emoji = getClusterEmoji(clusterIndex),
            description = getClusterDescription(clusterIndex),
            recommendation = getHealthRecommendation(clusterIndex),
            isHealthy = isHealthyCluster(clusterIndex),
            isDangerous = isDangerousCluster(clusterIndex),
            severity = getClusterSeverity(clusterIndex)
        )
    }

    /**
     * Data class untuk menyimpan informasi cluster lengkap
     */
    data class ClusterInfo(
        val index: Int,
        val color: Int,
        val name: String,
        val emoji: String,
        val description: String,
        val recommendation: String,
        val isHealthy: Boolean,
        val isDangerous: Boolean,
        val severity: String
    )
}