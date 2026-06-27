// ClusterResult.kt
package com.example.gocheck.model

enum class ClusterResult(
    val id: Int,
    val displayName: String,
    val emoji: String,
    val description: String
) {
    SUGAR_HIGH(0, "Makanan Manis", "⚠️", "Mengandung gula tinggi"),
    DENSE(1, "Makanan Padat", "⚠️", "Padat kalori"),
    HEALTHY(2, "Diet Sehat", "✅", "Pilihan sehat"),
    FIBER_HIGH(3, "Kaya Serat", "✅", "Tinggi serat"),
    SALT_HIGH(4, "Makanan Asin", "⚠️", "Mengandung garam tinggi")
}