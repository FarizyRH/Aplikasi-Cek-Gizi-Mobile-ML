package com.example.gocheck.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gocheck.adapter.HistoryAdapter
import com.example.gocheck.databinding.FragmentHistoryBinding
import com.example.gocheck.model.ScanHistory
import com.example.gocheck.repository.ScanRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistoryAdapter
    private lateinit var repository: ScanRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = ScanRepository(requireActivity().application)
        setupRecyclerView()
        loadHistoryData()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            items = emptyList(),
            onItemClick = { scan ->
                // opsi 2: kirim id saja ke DetailHistoryFragment
                val fragment = DetailHistoryFragment.newInstance(scan.id)
                parentFragmentManager.beginTransaction()
                    .replace(com.example.gocheck.R.id.fragment_container, fragment)
                    .addToBackStack("detail_history")
                    .commit()
            },
            onDeleteClick = { scan ->
                showDeleteConfirmation(scan)
            }
        )

        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter   // ← ini yang benar
        }
    }

    private fun loadHistoryData() {
        lifecycleScope.launch {
            repository.getAllScans().collectLatest { scans ->
                adapter.updateData(scans)
            }
        }
    }

    private fun showDeleteConfirmation(scan: ScanHistory) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Data?")
            .setMessage("Apakah kamu yakin ingin menghapus '${scan.foodName}' dari riwayat?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteScan(scan)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteScan(scan: ScanHistory) {
        lifecycleScope.launch {
            try {
                repository.deleteScan(scan)
                Toast.makeText(
                    requireContext(),
                    "✓ Data berhasil dihapus",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Gagal menghapus data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
