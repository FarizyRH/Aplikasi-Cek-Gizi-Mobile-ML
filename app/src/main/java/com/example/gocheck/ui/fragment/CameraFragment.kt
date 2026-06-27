package com.example.gocheck.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gocheck.R
import com.example.gocheck.databinding.FragmentKameraBinding
import com.example.gocheck.ml.OnnxHelper
import com.example.gocheck.model.NutritionResult
import com.example.gocheck.network.BarcodeService
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentKameraBinding? = null
    private val binding get() = _binding!!

    // Camera & Executor
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null

    // Services & Helpers
    private lateinit var barcodeService: BarcodeService
    private lateinit var onnxHelper: OnnxHelper

    // State Scanning
    private var isScanning = true
    private var lastScannedBarcode: String? = null

    // --- DATA CLUSTER (Untuk UI Warna-warni) ---
    private val clusterColors = mapOf(
        0 to R.color.cluster_danger_sugar, // Merah
        1 to R.color.cluster_neutral_dense, // Abu-abu
        2 to R.color.cluster_healthy_diet, // Biru Muda
        3 to R.color.cluster_special_fiber, // Hijau
        4 to R.color.cluster_warning_salt   // Kuning
    )

    private val clusterNames = mapOf(
        0 to "⚠️ Makanan Manis",
        1 to "⚠️ Makanan Padat",
        2 to "✅ Diet Sehat",
        3 to "✅ Kaya Serat",
        4 to "⚠️ Makanan Asin"
    )

    private val clusterDescriptions = mapOf(
        0 to "Tinggi gula. Kurangi untuk kesehatan gigi & berat badan.",
        1 to "Padat kalori. Konsumsi seimbang dengan aktivitas.",
        2 to "Pilihan baik untuk diet sehat.",
        3 to "Tinggi serat, bagus untuk pencernaan.",
        4 to "Tinggi garam. Batasi asupan harian."
    )

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "CameraFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Init Executor & Services
        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeService = BarcodeService(requireContext())
        onnxHelper = OnnxHelper(requireContext())

        // 2. Setup UI Listeners
        setupUI()

        // 3. Cek Izin & Mulai Kamera
        checkAndRequestPermissions()
    }

    private fun setupUI() {
        binding.apply {
            // Tombol Pause/Resume
            btnToggleScan.setOnClickListener { toggleScanning() }

            // Tombol Input Manual
            btnManualInput.setOnClickListener { navigateToManualInput() }

            // Tombol Retry (jika error)
            btnRetry.setOnClickListener { resetCamera() }

            // Tombol Scan Lagi (di layar hasil)
            btnScanAgain.setOnClickListener { resetCamera() }

            // Tombol Edit & Simpan (Kirim data ke InputFragment)
            // Listener ini akan di-update datanya saat hasil didapat
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(context, "Izin kamera diperlukan", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                // Image Analysis (Scanner)
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageForBarcode(imageProxy)
                        }
                    }

                // Bind ke Lifecycle
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )

                // UI Update
                binding.cameraStatus.text = "📷 Kamera siap"
                binding.errorContainer.visibility = View.GONE

            } catch (exc: Exception) {
                Log.e(TAG, "Gagal start kamera", exc)
                showError("Gagal membuka kamera: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processImageForBarcode(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // Setup ML Kit Barcode Scanner
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
                .build()
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null && rawValue != lastScannedBarcode) {
                        lastScannedBarcode = rawValue
                        isScanning = false // Stop scanning sementara
                        onBarcodeDetected(rawValue)
                        break
                    }
                }
            }
            .addOnFailureListener { Log.e(TAG, "Scan fail", it) }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun onBarcodeDetected(barcode: String) {
        Log.d(TAG, "Barcode found: $barcode")

        // Pindah ke Main Thread untuk update UI
        binding.previewView.post {
            binding.progressBar.visibility = View.VISIBLE
            binding.cameraStatus.text = "🔍 Mencari data produk..."
            fetchFoodDataFromBarcode(barcode)
        }
    }

    private fun fetchFoodDataFromBarcode(barcode: String) {
        lifecycleScope.launch {
            try {
                // Panggil BarcodeService
                val foodData = barcodeService.searchFoodByBarcode(barcode)

                if (foodData != null) {
                    showBarcodeResult(barcode, foodData)
                } else {
                    showError("Produk tidak ditemukan di database.")
                    // Reset otomatis setelah 2 detik
                    binding.previewView.postDelayed({ resetCamera() }, 2500)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetch", e)
                showError("Gagal mengambil data: ${e.message}")
            }
        }
    }

    private fun showBarcodeResult(barcode: String, foodData: Map<String, Any>) {
        // 1. Ekstrak Data
        val productName = foodData["product_name"] as? String ?: "Unknown"
        val brand = foodData["brands"] as? String ?: ""
        var displayName = if (brand.isNotEmpty()) "$productName ($brand)" else productName

        // Parsing Nutrisi
        val energy = (foodData["energy"] as? Number)?.toFloat() ?: 0f
        val protein = (foodData["protein"] as? Number)?.toFloat() ?: 0f
        val carbo = (foodData["carbohydrates"] as? Number)?.toFloat() ?: 0f
        val fat = (foodData["fat"] as? Number)?.toFloat() ?: 0f
        val sugar = (foodData["sugar"] as? Number)?.toFloat() ?: 0f
        val sodium = (foodData["sodium"] as? Number)?.toFloat() ?: 0f
        val fiber = (foodData["fiber"] as? Number)?.toFloat() ?: 0f

        // --- FILTER LOGIKA (KRITIKAL) ---
        // Jika nama produk Unknown ATAU (Energi 0 DAN Protein 0 DAN Lemak 0)
        // Kemungkinan besar ini adalah "Ghost Product" / Data Kosong.
        val isSuspiciousData = (energy == 0f && protein == 0f && fat == 0f && carbo == 0f)

        if (productName.contains("Unknown", ignoreCase = true) || isSuspiciousData) {
            // Jangan tampilkan sukses. Alihkan ke Error atau Manual Input.
            binding.progressBar.visibility = View.GONE
            binding.resultContainer.visibility = View.GONE

            // Tampilkan pesan error spesifik
            binding.errorContainer.visibility = View.VISIBLE
            binding.tvErrorMessage.text = "Produk ditemukan, tapi data nutrisi kosong di database.\nSilakan input manual."

            // Ubah tombol "Coba Lagi" menjadi "Input Manual" khusus untuk kasus ini
            binding.btnRetry.text = "✏️ Input Manual"
            binding.btnRetry.setOnClickListener {
                navigateToManualInput() // Atau kirim barcode yang ada ke form manual
            }
            return
        }
        // ---------------------------------

        binding.progressBar.visibility = View.GONE
        binding.previewView.visibility = View.INVISIBLE
        binding.resultContainer.visibility = View.VISIBLE

        binding.tvFoodName.text = displayName
        binding.tvBarcode.text = "Barcode: $barcode"

        // --- LOGIKA ONNX (Lanjut jika data valid) ---
        val inputData = floatArrayOf(energy, protein, carbo, fat, sugar, sodium, fiber)
        val prediction = performPrediction(inputData)

        // Update UI Berdasarkan Cluster
        val clusterIndex = prediction.clusterIndex
        val colorResId = clusterColors[clusterIndex] ?: R.color.cluster_neutral_dense
        val colorInt = ContextCompat.getColor(requireContext(), colorResId)

        binding.resultContainer.setBackgroundColor(colorInt)

        val clusterName = clusterNames[clusterIndex] ?: "Unknown"
        val clusterDesc = clusterDescriptions[clusterIndex] ?: "-"

        binding.tvNutritionInfo.text = buildString {
            append("KATEGORI: $clusterName\n")
            append("$clusterDesc\n")
            append("--------------------------------\n")
            append("Energi      : %.0f kcal\n".format(energy))
            append("Protein     : %.1f g\n".format(protein))
            append("Karbohidrat : %.1f g\n".format(carbo))
            append("Lemak       : %.1f g\n".format(fat))
            append("Gula        : %.1f g\n".format(sugar))
            append("Garam (Na)  : %.2f g\n".format(sodium))
            append("Serat       : %.1f g".format(fiber))
        }

        binding.btnSendToAnalyzer.setOnClickListener {
            sendToInputFragment(displayName, energy, protein, carbo, fat, sugar, sodium, fiber)
        }
    }

    private fun performPrediction(inputData: FloatArray): NutritionResult {
        return try {
            onnxHelper.predict(inputData)
        } catch (e: Exception) {
            NutritionResult(-1, 0f, floatArrayOf())
        }
    }

    private fun sendToInputFragment(
        name: String, energy: Float, protein: Float, carbo: Float,
        fat: Float, sugar: Float, sodium: Float, fiber: Float
    ) {
        val bundle = Bundle().apply {
            putString("food_name", name)
            putFloat("energy", energy)
            putFloat("protein", protein)
            putFloat("carbohydrates", carbo)
            putFloat("fat", fat)
            putFloat("sugar", sugar)
            putFloat("sodium", sodium)
            putFloat("fiber", fiber)
        }

        // Kirim result ke fragment tujuan
        parentFragmentManager.setFragmentResult("barcode_scan_result", bundle)

        // Navigasi
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, InputFragment().apply { arguments = bundle })
            .addToBackStack(null)
            .commit()
    }

    private fun toggleScanning() {
        isScanning = !isScanning
        binding.btnToggleScan.text = if (isScanning) "⏸ Pause" else "▶ Resume"
    }

    private fun navigateToManualInput() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, InputFragment())
            .addToBackStack(null)
            .commit()
    }

    fun resetCamera() {
        isScanning = true
        lastScannedBarcode = null

        // Reset UI ke mode Preview
        binding.progressBar.visibility = View.GONE
        binding.resultContainer.visibility = View.GONE
        binding.errorContainer.visibility = View.GONE

        // Tampilkan Preview kembali
        binding.previewView.visibility = View.VISIBLE
        binding.cameraStatus.text = "📷 Kamera siap"
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.resultContainer.visibility = View.GONE
        binding.errorContainer.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        if (::onnxHelper.isInitialized) {
            onnxHelper.close()
        }
        _binding = null
    }
}