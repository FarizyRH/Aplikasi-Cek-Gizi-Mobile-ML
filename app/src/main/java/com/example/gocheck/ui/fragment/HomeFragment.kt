package com.example.gocheck.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gocheck.databinding.FragmentHomeBinding
import com.example.gocheck.repository.ScanRepository
import com.example.gocheck.utils.FormatUtils // ✅ PENTING: Gunakan ini
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ScanRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = ScanRepository(requireActivity().application)

        setupUI()
        observeDailyData()
    }

    private fun setupUI() {
        // 1. Sapaan
        binding.tvGreeting.text = getGreeting()

        // 2. Tanggal Hari Ini (✅ Menggunakan FormatUtils)
        // Tidak perlu bikin SimpleDateFormat manual lagi
        binding.tvCurrentDate.text = FormatUtils.formatDateFull(System.currentTimeMillis())
    }

    private fun observeDailyData() {
        // Tentukan Range Waktu Hari Ini (00:00 - 23:59)
        val calendar = Calendar.getInstance()

        // Start of Day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.timeInMillis

        // End of Day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        lifecycleScope.launch {
            // Mengakses data berdasarkan range waktu
            // Pastikan scanHistoryDao di repository bersifat public,
            // atau gunakan repository.getScansToday(startOfDay, endOfDay) jika sudah dibuat.
            repository.getScansToday(startOfDay, endOfDay)
                .collectLatest { scans ->

                    if (scans.isEmpty()) {
                        showEmptyState()
                    } else {
                        var totalEnergy = 0f
                        var totalSugar = 0f
                        var totalSalt = 0f
                        var totalFat = 0f

                        for (item in scans) {
                            totalEnergy += item.energy
                            totalSugar += item.sugar
                            totalSalt += item.sodium
                            totalFat += item.fat
                        }

                        updateDashboard(totalEnergy, totalSugar, totalSalt, totalFat)

                        // Update Card Terakhir Discan
                        val lastItem = scans.first()
                        updateLastScanCard(lastItem.foodName, lastItem.timestamp, lastItem.energy)
                    }
                }
        }
    }

    private fun updateDashboard(energy: Float, sugar: Float, salt: Float, fat: Float) {
        binding.apply {
            // ✅ Menggunakan FormatUtils: Lebih Rapi & Konsisten
            tvTotalCalories.text = FormatUtils.formatNutrient(energy, "kcal", 0)
            tvTotalSugar.text = FormatUtils.formatNutrient(sugar, "g")
            tvTotalSalt.text = FormatUtils.formatNutrient(salt, "mg", 0)
            tvTotalFat.text = FormatUtils.formatNutrient(fat, "g")
        }
    }

    private fun updateLastScanCard(foodName: String, timestamp: Long, energy: Float) {
        binding.apply {
            tvLastFood.text = foodName
            // ✅ Format Kalori otomatis
            tvLastCal.text = FormatUtils.formatNutrient(energy, "kcal", 0)

            // ✅ Format Jam otomatis
            tvLastDate.text = "Pukul ${FormatUtils.formatTime(timestamp)}"
        }
    }

    private fun showEmptyState() {
        binding.apply {
            // ✅ Konsisten menggunakan format yang sama meski nilai 0
            tvTotalCalories.text = FormatUtils.formatNutrient(0f, "kcal", 0)
            tvTotalSugar.text = FormatUtils.formatNutrient(0f, "g")
            tvTotalSalt.text = FormatUtils.formatNutrient(0f, "mg", 0)
            tvTotalFat.text = FormatUtils.formatNutrient(0f, "g")

            tvLastFood.text = "Belum ada asupan hari ini"
            tvLastDate.text = "-"
            tvLastCal.text = ""
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> "Selamat Pagi, 🌅"
            in 11..14 -> "Selamat Siang, ☀️"
            in 15..18 -> "Selamat Sore, 🌆"
            else -> "Selamat Malam, 🌙"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}