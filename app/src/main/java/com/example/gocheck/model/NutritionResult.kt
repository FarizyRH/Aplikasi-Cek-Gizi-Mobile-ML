// NutritionResult.kt
package com.example.gocheck.model

data class NutritionResult(
    val clusterIndex: Int,           // 0-4
    val confidence: Float,           // 0-100
    val scores: FloatArray          // Scores untuk setiap cluster
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NutritionResult
        if (clusterIndex != other.clusterIndex) return false
        if (confidence != other.confidence) return false
        if (!scores.contentEquals(other.scores)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = clusterIndex
        result = 31 * result + confidence.hashCode()
        result = 31 * result + scores.contentHashCode()
        return result
    }
}