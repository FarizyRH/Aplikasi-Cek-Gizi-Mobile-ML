package com.example.gocheck.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gocheck.R
import com.example.gocheck.databinding.FragmentInputBinding
import com.example.gocheck.ml.OnnxHelper
import com.example.gocheck.model.NutritionResult
import com.example.gocheck.model.ScanHistory
import com.example.gocheck.repository.ScanRepository
import com.example.gocheck.utils.ValidationUtils
import kotlinx.coroutines.launch

class InputFragment : Fragment() {

    private var _binding: FragmentInputBinding? = null
    private val binding get() = _binding!!

    private lateinit var onnxHelper: OnnxHelper
    private lateinit var repository: ScanRepository

    // State hasil terakhir (untuk tombol Simpan)
    private var lastResult: NutritionResult? = null
    private var lastFoodName: String = ""
    private var lastInputs: FloatArray? = null

    // Data Cluster untuk pewarnaan hasil
    private val clusterColors = mapOf(
        0 to R.color.cluster_danger_sugar,
        1 to R.color.cluster_neutral_dense,
        2 to R.color.cluster_healthy_diet,
        3 to R.color.cluster_special_fiber,
        4 to R.color.cluster_warning_salt
    )

    private val clusterNames = mapOf(
        0 to "⚠️ Makanan Manis",
        1 to "⚠️ Makanan Padat",
        2 to "✅ Diet Sehat",
        3 to "✅ Kaya Serat",
        4 to "⚠️ Makanan Asin"
    )

    private val clusterDescriptions = mapOf(
        0 to "Produk ini mengandung kadar gula yang tinggi. Kurangi konsumsi gula untuk menjaga kesehatan gigi dan berat badan.",
        1 to "Makanan ini padat kalori. Konsumsi dalam porsi terkontrol dan seimbang dengan aktivitas fisik.",
        2 to "Pilihan yang baik untuk diet sehat. Terus pertahankan pola makan seperti ini untuk kesehatan optimal.",
        3 to "Produk ini tinggi serat, sangat bagus untuk pencernaan dan kesehatan usus Anda.",
        4 to "Produk ini mengandung kadar garam yang tinggi. Batasi asupan garam harian Anda ≤ 2000mg."
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onnxHelper = OnnxHelper(requireContext())
        repository = ScanRepository(requireActivity().application)

        // Jika ada arguments dari Bundle (misal dikirim manual saat fragment transaction)
        arguments?.let { args ->
            populateFormFromBarcode(args)
        }

        setupListeners()
        setupBarcodeResultListener()
    }

    private fun setupListeners() {
        binding.btnAnalyze.setOnClickListener {
            analyzeNutrition(isAuto = false)
        }

        // Tombol "Simpan ke Riwayat" di card_result
        binding.btnSaveHistory.setOnClickListener {
            saveIfAvailable()
        }
    }

    /**
     * Setup listener untuk menerima hasil scan dari CameraFragment
     * menggunakan Fragment Result API
     */
    private fun setupBarcodeResultListener() {
        parentFragmentManager.setFragmentResultListener(
            "barcode_scan_result",
            viewLifecycleOwner
        ) { _, bundle ->
            populateFormFromBarcode(bundle)
            Toast.makeText(
                context,
                "📊 Data dari barcode telah dimuat",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Isi form dengan data dari barcode/bundle DAN lakukan analisis otomatis
     */
    private fun populateFormFromBarcode(bundle: Bundle) {
        binding.apply {
            bundle.getString("food_name")?.let { name ->
                if (name.isNotEmpty()) etFoodName.setText(name)
            }

            // Isi form hanya jika nilai > 0
            bundle.getFloat("energy", 0f).let { if (it > 0) etEnergy.setText(it.toString()) }
            bundle.getFloat("protein", 0f).let { if (it > 0) etProtein.setText(it.toString()) }
            bundle.getFloat("carbohydrates", 0f).let { if (it > 0) etCarbo.setText(it.toString()) }
            bundle.getFloat("fat", 0f).let { if (it > 0) etFat.setText(it.toString()) }
            bundle.getFloat("sugar", 0f).let { if (it > 0) etSugar.setText(it.toString()) }
            bundle.getFloat("sodium", 0f).let { if (it > 0) etSodium.setText(it.toString()) }
            bundle.getFloat("fiber", 0f).let { if (it > 0) etFiber.setText(it.toString()) }
        }

        // Jalankan analisis otomatis setelah UI selesai merender
        binding.root.post {
            analyzeNutrition(isAuto = true)
        }
    }

    private fun validateInputs(): Boolean {
        val energy = binding.etEnergy.text.toString().trim()
        val protein = binding.etProtein.text.toString().trim()
        val carbo = binding.etCarbo.text.toString().trim()
        val fat = binding.etFat.text.toString().trim()
        val sugar = binding.etSugar.text.toString().trim()
        val sodium = binding.etSodium.text.toString().trim()
        val fiber = binding.etFiber.text.toString().trim()

        return when {
            !ValidationUtils.isValidNumber(energy) -> {
                binding.tilEnergy.error = "Masukkan nilai energy yang valid"
                false
            }
            !ValidationUtils.isValidNumber(protein) -> {
                binding.tilProtein.error = "Masukkan nilai protein yang valid"
                false
            }
            !ValidationUtils.isValidNumber(carbo) -> {
                binding.tilCarbo.error = "Masukkan nilai carbo yang valid"
                false
            }
            !ValidationUtils.isValidNumber(fat) -> {
                binding.tilFat.error = "Masukkan nilai fat yang valid"
                false
            }
            !ValidationUtils.isValidNumber(sugar) -> {
                binding.tilSugar.error = "Masukkan nilai sugar yang valid"
                false
            }
            !ValidationUtils.isValidNumber(sodium) -> {
                binding.tilSodium.error = "Masukkan nilai sodium yang valid"
                false
            }
            !ValidationUtils.isValidNumber(fiber) -> {
                binding.tilFiber.error = "Masukkan nilai fiber yang valid"
                false
            }
            else -> true
        }
    }

    private fun analyzeNutrition(isAuto: Boolean = false) {
        // Clear errors
        binding.tilEnergy.error = null
        binding.tilProtein.error = null
        binding.tilCarbo.error = null
        binding.tilFat.error = null
        binding.tilSugar.error = null
        binding.tilSodium.error = null
        binding.tilFiber.error = null
        binding.tilFoodName.error = null

        if (!validateInputs()) {
            // Jika otomatis dan gagal validasi (misal data 0 semua),
            // jangan tampilkan toast error, biarkan user isi manual
            if (!isAuto) {
                Toast.makeText(context, "Periksa kembali input Anda", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Ambil nama makanan
        val foodName = binding.etFoodName.text.toString().trim()
        if (foodName.isEmpty()) {
            if (!isAuto) {
                binding.tilFoodName.error = "Masukkan nama makanan"
                Toast.makeText(context, "Masukkan nama makanan terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // --- PERBAIKAN KRITIKAL: Safe Parsing ---
        // Menggunakan toFloatOrNull() ?: 0f agar tidak crash jika input kosong/invalid
        val energy = binding.etEnergy.text.toString().toFloatOrNull() ?: 0f
        val protein = binding.etProtein.text.toString().toFloatOrNull() ?: 0f
        val carbo = binding.etCarbo.text.toString().toFloatOrNull() ?: 0f
        val fat = binding.etFat.text.toString().toFloatOrNull() ?: 0f
        val sugar = binding.etSugar.text.toString().toFloatOrNull() ?: 0f
        val sodium = binding.etSodium.text.toString().toFloatOrNull() ?: 0f
        val fiber = binding.etFiber.text.toString().toFloatOrNull() ?: 0f

        val inputData = floatArrayOf(energy, protein, carbo, fat, sugar, sodium, fiber)

        // Simpan state untuk tombol simpan
        lastFoodName = foodName
        lastInputs = inputData

        // ONNX Prediction
        val result = performPrediction(inputData)
        lastResult = result

        // Tampilkan hasil
        displayResult(result, energy, protein, carbo, fat, sugar, sodium, fiber)

        // --- TAMBAHAN UX: Auto Scroll ---
        if (isAuto) {
            binding.root.postDelayed({
                try {
                    binding.cardResult.parent.requestChildFocus(binding.cardResult, binding.cardResult)
                } catch (e: Exception) {
                    e.printStackTrace() // Cegah crash jika view belum siap
                }
            }, 200)
        }
    }

    private fun performPrediction(inputData: FloatArray): NutritionResult {
        return try {
            onnxHelper.predict(inputData)
        } catch (e: Exception) {
            Toast.makeText(context, "Error AI: ${e.message}", Toast.LENGTH_SHORT).show()
            NutritionResult(-1, 0f, floatArrayOf())
        }
    }

    private fun displayResult(
        result: NutritionResult,
        energy: Float,
        protein: Float,
        carbo: Float,
        fat: Float,
        sugar: Float,
        sodium: Float,
        fiber: Float
    ) {
        binding.cardResult.visibility = View.VISIBLE

        val clusterIndex = result.clusterIndex
        val clusterName = clusterNames[clusterIndex] ?: "Unknown"
        val clusterDesc = clusterDescriptions[clusterIndex] ?: "No description"
        val colorResId = clusterColors[clusterIndex] ?: R.color.cluster_neutral_dense

        val backgroundColor = resources.getColor(colorResId, null)
        binding.resultContainer.setBackgroundColor(backgroundColor)

        binding.tvClusterName.text = clusterName
        binding.tvResultDescription.text = clusterDesc

        binding.tvResultEnergy.text = "%.0f kcal".format(energy)
        binding.tvResultProtein.text = "%.1f g".format(protein)
        binding.tvResultCarbo.text = "%.1f g".format(carbo)
        binding.tvResultFat.text = "%.1f g".format(fat)

        if (!binding.cardResult.hasFocus()) {
            binding.cardResult.requestFocus()
        }
    }

    // Dipanggil saat tombol "Simpan ke Riwayat" ditekan
    private fun saveIfAvailable() {
        val result = lastResult
        val inputs = lastInputs
        val foodName = lastFoodName

        if (result == null || inputs == null || foodName.isEmpty()) {
            Toast.makeText(context, "Lakukan analisis terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val energy = inputs[0]
        val protein = inputs[1]
        val carbo = inputs[2]
        val fat = inputs[3]
        val sugar = inputs[4]
        val sodium = inputs[5]
        val fiber = inputs[6]

        saveToDatabaseAsync(result, foodName, energy, protein, carbo, fat, sugar, sodium, fiber)
    }

    private fun saveToDatabaseAsync(
        result: NutritionResult,
        foodName: String,
        energy: Float,
        protein: Float,
        carbo: Float,
        fat: Float,
        sugar: Float,
        sodium: Float,
        fiber: Float
    ) {
        lifecycleScope.launch {
            try {
                val scan = ScanHistory(
                    foodName = foodName,
                    energy = energy,
                    protein = protein,
                    carbohydrates = carbo,
                    fat = fat,
                    sugar = sugar,
                    sodium = sodium,
                    fiber = fiber,
                    clusterIndex = result.clusterIndex,
                    confidence = 100f,
                    timestamp = System.currentTimeMillis(),
                    description = clusterNames[result.clusterIndex] ?: "Unknown"
                )
                repository.insertScan(scan)
                Toast.makeText(context, "✓ Data disimpan ke history", Toast.LENGTH_SHORT).show()

                // Setelah simpan, bersihkan form
                clearForm()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearForm() {
        binding.apply {
            etFoodName.text?.clear()
            etEnergy.text?.clear()
            etProtein.text?.clear()
            etCarbo.text?.clear()
            etFat.text?.clear()
            etSugar.text?.clear()
            etSodium.text?.clear()
            etFiber.text?.clear()
            cardResult.visibility = View.GONE
        }
        lastResult = null
        lastInputs = null
        lastFoodName = ""

        // Scroll kembali ke atas setelah clear
        binding.root.post {
            binding.root.scrollTo(0, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::onnxHelper.isInitialized) {
            onnxHelper.close()
        }
    }
}