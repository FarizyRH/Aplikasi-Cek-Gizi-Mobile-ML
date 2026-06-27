package com.example.gocheck.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gocheck.R
import com.example.gocheck.databinding.ItemHistoryBinding
import com.example.gocheck.model.ScanHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private var items: List<ScanHistory>,
    private val onItemClick: (ScanHistory) -> Unit,    // klik card / item
    private val onDeleteClick: (ScanHistory) -> Unit   // klik tombol delete
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScanHistory) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val formattedDate = dateFormat.format(Date(item.timestamp))

            // Ambil warna berdasarkan cluster
            val colorResId = when (item.clusterIndex) {
                0 -> R.color.cluster_danger_sugar
                1 -> R.color.cluster_neutral_dense
                2 -> R.color.cluster_healthy_diet
                3 -> R.color.cluster_special_fiber
                4 -> R.color.cluster_warning_salt
                else -> R.color.cluster_neutral_dense
            }

            binding.apply {
                // Nama makanan dari database
                tvFoodName.text = item.foodName

                // Kategori cluster (tanpa confidence)
                tvResult.text = item.description

                // Tanggal
                tvDate.text = formattedDate

                // Warna dot cluster
                val color = root.context.resources.getColor(colorResId, null)
                viewClusterDot.background.setTint(color)

                // Klik card / item → buka detail
                root.setOnClickListener {
                    onItemClick(item)
                }

                // Klik tombol delete → hapus
                btnDelete.setOnClickListener {
                    onDeleteClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ScanHistory>) {
        items = newItems
        notifyDataSetChanged()
    }
}
