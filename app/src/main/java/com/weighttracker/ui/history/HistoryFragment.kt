package com.weighttracker.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.weighttracker.R
import com.weighttracker.data.Weight
import com.weighttracker.databinding.DialogEditWeightBinding
import com.weighttracker.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: WeightHistoryAdapter

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

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = WeightHistoryAdapter(
            onEditClick = { weight -> showEditDialog(weight) },
            onDeleteClick = { weight -> showDeleteDialog(weight) }
        )

        binding.rvHistory.apply {
            adapter = this@HistoryFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        viewModel.allWeights.observe(viewLifecycleOwner) { weights ->
            adapter.submitList(weights)
            
            if (weights.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
            }
        }
    }

    private fun showEditDialog(weight: Weight) {
        val dialogBinding = DialogEditWeightBinding.inflate(layoutInflater)
        dialogBinding.etEditWeight.setText(weight.weight.toString())

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val newWeightText = dialogBinding.etEditWeight.text.toString().trim()
            val newWeight = newWeightText.toFloatOrNull()

            if (newWeight == null || newWeight <= 0 || newWeight > 1000) {
                dialogBinding.tilEditWeight.error = getString(R.string.invalid_weight)
                return@setOnClickListener
            }

            val updatedWeight = weight.copy(weight = newWeight)
            viewModel.updateWeight(updatedWeight)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteDialog(weight: Weight) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteWeight(weight)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 