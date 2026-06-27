package com.example.gocheck.network

import android.content.Context
import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class BarcodeService(private val context: Context) {

    companion object {
        private const val TAG = "BarcodeService"

        // GANTI INI: Gunakan .org dan API v0 yang lebih stabil untuk direct JSON
        private const val OPENFOODFACTS_BASE_URL = "https://world.openfoodfacts.org/api/v0/product"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Search food by barcode using OpenFoodFacts API
     * ✅ NO OAuth needed!
     * ✅ FREE API!
     * ✅ Simple to use!
     *
     * @param barcode 13-digit GTIN-13/EAN-13 barcode
     * @return Map containing food info + nutrition data
     */
    suspend fun searchFoodByBarcode(barcode: String): Map<String, Any>? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔍 Searching barcode: $barcode")

                // OpenFoodFacts endpoint - VERY SIMPLE!
                val url = "$OPENFOODFACTS_BASE_URL/$barcode.json"
                Log.d(TAG, "📡 Request URL: $url")

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("User-Agent", "GoCheck/1.0")
                    .build()

                val response = httpClient.newCall(request).execute()
                Log.d(TAG, "📊 Response Code: ${response.code}")

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e(TAG, "❌ API Error: ${response.code} - $errorBody")
                    null
                } else {
                    val body = response.body?.string()
                    if (body == null) {
                        Log.e(TAG, "❌ Response body is null")
                        null
                    } else {
                        Log.d(TAG, "📄 Response Body (first 200 chars): ${body.take(200)}")
                        parseBarcodeResponse(body, barcode)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error searching food by barcode", e)
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Parse OpenFoodFacts response
     * Response structure: {"product": {...nutrition data...}}
     */
    private fun parseBarcodeResponse(jsonString: String, barcode: String): Map<String, Any>? {
        return try {
            val json = JsonParser.parseString(jsonString).asJsonObject

            // Check for error
            if (json.has("error")) {
                Log.e(TAG, "❌ OpenFoodFacts Error: ${json.get("error")}")
                return null
            }

            // OpenFoodFacts response: {"product": {...}}
            if (!json.has("product")) {
                Log.w(TAG, "⚠️ No 'product' in response - barcode not found")
                return null
            }

            val product = json.getAsJsonObject("product")
            val productName = product.get("product_name")?.asString ?: "Unknown Product"
            val brands = product.get("brands")?.asString ?: "Unknown Brand"

            Log.d(TAG, "✅ Found: $productName by $brands")

            // Extract nutrition data from nutriments
            val nutriments = if (product.has("nutriments")) {
                product.getAsJsonObject("nutriments")
            } else {
                null
            }

            // Extract values with safe defaults (per 100g)
            val calories = nutriments?.get("energy-kcal_100g")?.asFloat
                ?: nutriments?.get("energy_100g")?.asFloat?.div(4.184f) // Convert kJ to kcal
                ?: 0f
            val protein = nutriments?.get("proteins_100g")?.asFloat ?: 0f
            val carbs = nutriments?.get("carbohydrates_100g")?.asFloat ?: 0f
            val fat = nutriments?.get("fat_100g")?.asFloat ?: 0f
            val sugar = nutriments?.get("sugars_100g")?.asFloat ?: 0f
            val sodium = nutriments?.get("sodium_100g")?.asFloat ?: 0f
            val fiber = nutriments?.get("fiber_100g")?.asFloat ?: 0f

            Log.d(TAG, "✅ Nutrition (per 100g) - Cal:$calories, Protein:$protein, Carbs:$carbs")

            // ✅ Return complete nutrition map
            mapOf(
                "product_name" to productName,
                "brands" to brands,
                "barcode" to barcode,
                "calories" to calories,
                "energy" to calories,  // Same as calories
                "protein" to protein,
                "carbohydrates" to carbs,
                "fat" to fat,
                "sugar" to sugar,
                "sodium" to sodium,
                "fiber" to fiber,
                "source" to "OpenFoodFacts"  // Track data source
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing barcode response", e)
            e.printStackTrace()
            null
        }
    }
    // ... kode lama ...

    /**
     * Cari produk berdasarkan nama (Query Search)
     * URL: https://world.openfoodfacts.org/cgi/search.pl?search_terms={query}&search_simple=1&action=process&json=1
     */
    suspend fun searchProductsByName(query: String): List<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔍 Searching query: $query")

                // URL Construction
                val url = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=$query&search_simple=1&action=process&json=1&page_size=20"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("User-Agent", "GoCheck/1.0")
                    .build()

                val response = httpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e(TAG, "❌ API Search Error: ${response.code}")
                    return@withContext emptyList()
                }

                val body = response.body?.string()
                if (body == null) return@withContext emptyList()

                parseSearchResponse(body)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error searching product", e)
                emptyList()
            }
        }
    }

    private fun parseSearchResponse(jsonString: String): List<Map<String, Any>> {
        val results = mutableListOf<Map<String, Any>>()
        try {
            val json = JsonParser.parseString(jsonString).asJsonObject

            if (!json.has("products")) return emptyList()

            val productsArray = json.getAsJsonArray("products")

            for (element in productsArray) {
                val product = element.asJsonObject

                // Ambil data dasar
                val productName = product.get("product_name")?.asString ?: "Unknown"
                val brands = product.get("brands")?.asString ?: ""
                val id = product.get("code")?.asString ?: "" // Barcode sebagai ID unik

                // Ambil Nutrisi (Safe Parsing)
                val nutriments = if (product.has("nutriments")) product.getAsJsonObject("nutriments") else null

                val energy = nutriments?.get("energy-kcal_100g")?.asFloat ?: 0f
                val sugar = nutriments?.get("sugars_100g")?.asFloat ?: 0f
                // ... (bisa tambah nutrisi lain jika mau ditampilkan di list) ...

                // Masukkan ke Map
                val item = mapOf(
                    "id" to id, // Barcode
                    "name" to productName,
                    "brand" to brands,
                    "energy" to energy,
                    "sugar" to sugar
                    // Nanti saat diklik, kita bisa fetch ulang detail lengkap pakai barcode-nya
                )

                // Filter: Hanya tampilkan yang punya nama jelas
                if (productName != "Unknown") {
                    results.add(item)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing search results", e)
        }
        return results
    }
}