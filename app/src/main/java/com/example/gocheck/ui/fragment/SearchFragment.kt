package com.example.gocheck.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gocheck.R
import com.example.gocheck.adapter.SearchAdapter
import com.example.gocheck.databinding.FragmentSearchBinding
import com.example.gocheck.network.BarcodeService
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var barcodeService: BarcodeService
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeService = BarcodeService(requireContext())

        setupUI()
    }

    private fun setupUI() {
        // Setup RecyclerView
        searchAdapter = SearchAdapter { barcode ->
            // Callback saat item diklik:
            // Kita gunakan barcode dari item tersebut untuk "pura-pura" seolah hasil scan kamera
            processSelectedProduct(barcode)
        }

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }

        // Setup SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    performSearch(query)
                    binding.searchView.clearFocus() // Tutup keyboard
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Tidak melakukan autocomplete agar hemat kuota/request
                return false
            }
        })
    }

    private fun performSearch(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE

        lifecycleScope.launch {
            val results = barcodeService.searchProductsByName(query)

            binding.progressBar.visibility = View.GONE

            if (results.isNotEmpty()) {
                binding.rvSearchResults.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
                binding.noResultsState.visibility = View.GONE
                searchAdapter.submitList(results)
            } else {
                binding.rvSearchResults.visibility = View.GONE
                binding.tvEmptyState.visibility = View.GONE
                binding.noResultsState.visibility = View.VISIBLE
            }
        }

    }

    private fun processSelectedProduct(barcode: String) {
        // Saat user klik produk, kita "lempar" barcodenya ke CameraFragment logic
        // Tapi karena logic fetch API ada di Camera/Input,
        // kita bisa fetch detailnya dulu di sini, lalu kirim ke InputFragment.

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val fullData = barcodeService.searchFoodByBarcode(barcode) // Reuse fungsi lama!
            binding.progressBar.visibility = View.GONE

            if (fullData != null) {
                sendToInputFragment(fullData)
            } else {
                Toast.makeText(context, "Gagal mengambil detail produk", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendToInputFragment(foodData: Map<String, Any>) {
        val bundle = Bundle().apply {
            val productName = foodData["product_name"] as? String ?: ""
            putString("food_name", productName)
            putFloat("energy", (foodData["energy"] as? Number)?.toFloat() ?: 0f)
            putFloat("protein", (foodData["protein"] as? Number)?.toFloat() ?: 0f)
            putFloat("carbohydrates", (foodData["carbohydrates"] as? Number)?.toFloat() ?: 0f)
            putFloat("fat", (foodData["fat"] as? Number)?.toFloat() ?: 0f)
            putFloat("sugar", (foodData["sugar"] as? Number)?.toFloat() ?: 0f)
            putFloat("sodium", (foodData["sodium"] as? Number)?.toFloat() ?: 0f)
            putFloat("fiber", (foodData["fiber"] as? Number)?.toFloat() ?: 0f)
        }

        // Gunakan Fragment Result API (sama seperti CameraFragment)
        parentFragmentManager.setFragmentResult("barcode_scan_result", bundle)

        // Pindah ke InputFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, InputFragment())
            .addToBackStack(null)
            .commit()

        // Update Bottom Nav (Optional, biar UI sync)
        // (Biasanya di MainActivity ada logic update bottom nav, tapi ini minor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}