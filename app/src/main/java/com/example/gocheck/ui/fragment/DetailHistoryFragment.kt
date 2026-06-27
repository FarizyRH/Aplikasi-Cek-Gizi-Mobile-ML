package com.example.gocheck.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.gocheck.R
import com.example.gocheck.databinding.FragmentDetailHistoryBinding
import com.example.gocheck.model.ScanHistory
import com.example.gocheck.repository.ScanRepository
import com.example.gocheck.utils.ClusterHelper
import com.example.gocheck.utils.DateFormatUtil
import com.example.gocheck.utils.ShareResultUtil
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Detail History Fragment menggunakan BaseFragment
 * Otomatis handle bottom navigation padding dan window insets
 */
class DetailHistoryFragment : BaseFragment() {

    private var _binding: FragmentDetailHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var scanRepository: ScanRepository
    private var scanHistory: ScanHistory? = null
    private var scanId: Int = -1

    // Enable window insets handling
    override val enableWindowInsets = true

    // Specify views yang perlu bottom padding
    override val viewsNeedingBottomPadding: List<View>
        get() = if (_binding != null) listOf(binding.actionButtons) else emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanId = arguments?.getInt(ARG_SCAN_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanRepository = ScanRepository(requireActivity().application)
        setupToolbar()

        if (scanId == -1) {
            showErrorAndGoBack("Data tidak ditemukan")
            return
        }

        loadScanData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadScanData() {
        showLoading()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val scan = scanRepository.getScanById(scanId)
                hideLoading()

                if (scan == null) {
                    showErrorAndGoBack("Data tidak ditemukan")
                } else {
                    scanHistory = scan
                    displayDetailHistory(scan)
                    setupClickListeners(scan)
                }
            } catch (e: Exception) {
                hideLoading()
                showErrorAndGoBack("Error: ${e.message}")
            }
        }
    }

    private fun displayDetailHistory(scan: ScanHistory) {
        setupClusterCard(scan)
        setupProductInfoCard(scan)
        setupNutritionDetailsCard(scan)
        setupHealthRecommendationCard(scan)
    }

    private fun setupClusterCard(scan: ScanHistory) {
        val clusterInfo = ClusterHelper.getClusterInfo(requireContext(), scan.clusterIndex)

        binding.cardResultContainer.setCardBackgroundColor(clusterInfo.color)
        binding.tvClusterName.text = clusterInfo.name
        binding.tvClusterDescription.text = clusterInfo.description

        val confidenceFormat = DecimalFormat("#.#")
        binding.tvConfidenceScore.text = "Confidence: ${confidenceFormat.format(scan.confidence)}%"
    }

    private fun setupProductInfoCard(scan: ScanHistory) {
        binding.tvFoodNameDetail.text = scan.foodName.ifEmpty { "Produk Tidak Diketahui" }

        val scanDate = Date(scan.timestamp)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        binding.tvScanDateTime.text = dateFormat.format(scanDate)
        binding.tvElapsedTime.text = DateFormatUtil.getRelativeTime(scan.timestamp)
    }

    private fun setupNutritionDetailsCard(scan: ScanHistory) {
        val decimalFormat = DecimalFormat("#.#")

        val nutritionText = buildString {
            append("Energy: ${decimalFormat.format(scan.energy)} kcal\n")
            append("Protein: ${decimalFormat.format(scan.protein)} g\n")
            append("Carbohydrates: ${decimalFormat.format(scan.carbohydrates)} g\n")
            append("Fat: ${decimalFormat.format(scan.fat)} g\n")
            append("Sugar: ${decimalFormat.format(scan.sugar)} g\n")
            append("Sodium: ${decimalFormat.format(scan.sodium)} mg\n")
            append("Fiber: ${decimalFormat.format(scan.fiber)} g")
        }

        binding.tvNutritionDetails.text = nutritionText

        val totalCalories = scan.energy.toInt()
        val totalMacros = (scan.protein + scan.carbohydrates + scan.fat).toInt()
        binding.tvNutritionSummary.text =
            "Total Kalori: $totalCalories kcal | Total Makronutrisi: $totalMacros g"
    }

    private fun setupHealthRecommendationCard(scan: ScanHistory) {
        val recommendation = ClusterHelper.getHealthRecommendation(scan.clusterIndex)
        binding.tvHealthRecommendation.text = recommendation
    }

    private fun setupClickListeners(scan: ScanHistory) {
        binding.btnShareDetail.setOnClickListener {
            shareResult(scan)
        }

        binding.btnDeleteDetail.setOnClickListener {
            showDeleteConfirmationDialog(scan)
        }
    }

    private fun shareResult(scan: ScanHistory) {
        val shareText = ShareResultUtil.generateShareText(
            scan = scan,
            clusterNames = ClusterHelper.clusterNames,
            clusterDescriptions = ClusterHelper.clusterDescriptions,
            healthRecommendations = ClusterHelper.healthRecommendations
        )

        ShareResultUtil.shareViaApps(
            context = requireContext(),
            text = shareText,
            title = "Bagikan Hasil Scan Nutrisi"
        )
    }

    private fun showDeleteConfirmationDialog(scan: ScanHistory) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Data?")
            .setMessage("Apakah Anda yakin ingin menghapus data scan untuk '${scan.foodName}'? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                deleteScan(scan)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteScan(scan: ScanHistory) {
        lifecycleScope.launch {
            try {
                scanRepository.deleteScan(scan)
                Toast.makeText(
                    requireContext(),
                    "✓ Data berhasil dihapus",
                    Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
            } catch (e: Exception) {
                showError("❌ Gagal menghapus data: ${e.message}")
            }
        }
    }

    private fun showErrorAndGoBack(message: String) {
        showError(message)
        parentFragmentManager.popBackStack()
    }

    // Override BaseFragment methods
    override fun showLoading() {
        // Show loading indicator if you have one
        // binding.progressBar?.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        // Hide loading indicator
        // binding.progressBar?.visibility = View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SCAN_ID = "arg_scan_id"

        fun newInstance(scanId: Int): DetailHistoryFragment {
            return DetailHistoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SCAN_ID, scanId)
                }
            }
        }
    }
}