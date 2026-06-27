package com.example.gocheck.ml

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.gocheck.model.NutritionResult
import java.nio.FloatBuffer

class OnnxHelper(private val context: Context) {

    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null
    private val tag = "OnnxHelper"

    init {
        try {
            loadModel("model_cluster.onnx")  // atau nama file model kamu
        } catch (e: Exception) {
            Log.e(tag, "Error loading model: ${e.message}")
        }
    }

    fun loadModel(modelName: String) {
        try {
            val modelBytes = context.assets.open(modelName).readBytes()
            val sessionOptions = OrtSession.SessionOptions().apply {
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            }
            session = env.createSession(modelBytes, sessionOptions)
            Log.d(tag, "Model loaded successfully: $modelName")
        } catch (e: Exception) {
            Log.e(tag, "Error loading model $modelName: ${e.message}")
            throw RuntimeException("Failed to load ONNX model: ${e.message}")
        }
    }

    fun predict(input: FloatArray): NutritionResult {
        val currentSession = session
            ?: throw RuntimeException("Model belum di-load. Panggil loadModel() terlebih dahulu.")

        if (input.size != 7) {
            throw IllegalArgumentException(
                "Input harus memiliki 7 elemen: [Energy, Protein, Carbo, Fat, Sugar, Sodium, Fiber]"
            )
        }

        return try {
            // Bentuk input: [1, 7]
            val inputShape = longArrayOf(1, 7)
            val buffer = FloatBuffer.allocate(7)
            buffer.put(input)
            buffer.rewind()

            val inputTensor = OnnxTensor.createTensor(env, buffer, inputShape)

            // Nama input HARUS "float_input" sesuai model ONNX
            val outputs = currentSession.run(mapOf("float_input" to inputTensor))

            // KMeans hanya mengeluarkan label cluster (int64/long)
            val rawOutput = outputs[0].value

            val clusterIndex: Int = when (rawOutput) {
                is LongArray -> rawOutput[0].toInt()
                is IntArray -> rawOutput[0]
                is Array<*> -> {
                    // Kalau di-wrap dalam array, ambil elemen pertama
                    when (val v = rawOutput.getOrNull(0)) {
                        is Long -> (v as Long).toInt()
                        is Int -> v as Int
                        is LongArray -> (v[0]).toInt()
                        is IntArray -> (v[0])
                        else -> -1
                    }
                }
                else -> {
                    Log.e(tag, "Unexpected output type: ${rawOutput?.javaClass}")
                    -1
                }
            }

            // KMeans tidak punya output skor, jadi confidence dan scores isi dummy
            val scores = floatArrayOf()
            val confidence = 100f

            // Tutup resource
            outputs.close()
            inputTensor.close()

            Log.d(tag, "Prediction: cluster=$clusterIndex, confidence=$confidence")

            NutritionResult(
                clusterIndex = clusterIndex,
                confidence = confidence,
                scores = scores
            )
        } catch (e: Exception) {
            Log.e(tag, "Error during prediction: ${e.message}")
            e.printStackTrace()
            NutritionResult(
                clusterIndex = -1,
                confidence = 0f,
                scores = floatArrayOf()
            )
        }
    }

    fun isModelLoaded(): Boolean = session != null

    fun close() {
        try {
            session?.close()
            env.close()
            Log.d(tag, "ONNX resources closed")
        } catch (e: Exception) {
            Log.e(tag, "Error closing ONNX: ${e.message}")
        }
    }
}
