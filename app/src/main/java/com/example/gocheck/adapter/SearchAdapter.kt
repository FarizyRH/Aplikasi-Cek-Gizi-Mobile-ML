package com.example.gocheck.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gocheck.databinding.ItemSearchBinding

class SearchAdapter(
    private val onItemClick: (String) -> Unit // Mengirim Barcode saat diklik
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private val items = mutableListOf<Map<String, Any>>()

    fun submitList(newItems: List<Map<String, Any>>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class SearchViewHolder(private val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Map<String, Any>) {
            val name = item["name"] as String
            val brand = item["brand"] as String
            val barcode = item["id"] as String
            val energy = (item["energy"] as? Number)?.toFloat() ?: 0f

            binding.tvProductName.text = name
            binding.tvBrand.text = if (brand.isNotEmpty()) brand else "Tanpa Merk"
            binding.tvCalories.text = "%.0f kcal".format(energy)

            binding.root.setOnClickListener {
                onItemClick(barcode)
            }
        }
    }
}