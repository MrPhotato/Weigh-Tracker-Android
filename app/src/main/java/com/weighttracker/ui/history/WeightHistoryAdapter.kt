package com.weighttracker.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weighttracker.data.Weight
import com.weighttracker.databinding.ItemWeightHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class WeightHistoryAdapter(
    private val onEditClick: (Weight) -> Unit,
    private val onDeleteClick: (Weight) -> Unit
) : ListAdapter<Weight, WeightHistoryAdapter.WeightViewHolder>(WeightDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val binding = ItemWeightHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeightViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WeightViewHolder(
        private val binding: ItemWeightHistoryBinding,
        private val onEditClick: (Weight) -> Unit,
        private val onDeleteClick: (Weight) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(weight: Weight) {
            binding.tvWeight.text = "${weight.weight} kg"
            binding.tvDate.text = dateFormat.format(weight.date)

            binding.btnEdit.setOnClickListener { onEditClick(weight) }
            binding.btnDelete.setOnClickListener { onDeleteClick(weight) }
        }
    }

    class WeightDiffCallback : DiffUtil.ItemCallback<Weight>() {
        override fun areItemsTheSame(oldItem: Weight, newItem: Weight): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Weight, newItem: Weight): Boolean {
            return oldItem == newItem
        }
    }
} 