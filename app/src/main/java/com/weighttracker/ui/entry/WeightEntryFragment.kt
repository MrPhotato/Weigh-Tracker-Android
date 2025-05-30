package com.weighttracker.ui.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.weighttracker.R
import com.weighttracker.databinding.FragmentWeightEntryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class WeightEntryFragment : Fragment() {

    private var _binding: FragmentWeightEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeightEntryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            val weightText = binding.etWeight.text.toString().trim()
            
            if (weightText.isEmpty()) {
                binding.tilWeight.error = getString(R.string.invalid_weight)
                return@setOnClickListener
            }

            val weight = weightText.toFloatOrNull()
            if (weight == null || weight <= 0 || weight > 1000) {
                binding.tilWeight.error = getString(R.string.invalid_weight)
                return@setOnClickListener
            }

            binding.tilWeight.error = null
            
            // 保存体重（暂时不支持备注）
            viewModel.saveWeight(weight, "")
        }
    }

    private fun observeViewModel() {
        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                val message = if (viewModel.hasTodayWeight()) {
                    getString(R.string.weight_updated)
                } else {
                    getString(R.string.weight_saved)
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                
                // 如果是新记录，清空输入框；如果是更新，保持当前值
                if (!viewModel.hasTodayWeight()) {
                    binding.etWeight.text?.clear()
                }
            } else {
                Toast.makeText(context, getString(R.string.error_saving), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.todayWeight.observe(viewLifecycleOwner) { weight ->
            if (weight != null) {
                // 今天已有记录，显示当前值并更新UI状态
                binding.tvTodayWeight.text = getString(R.string.today_weight, weight.weight)
                binding.btnSave.text = getString(R.string.update_weight)
                
                // 预填充当前值以便编辑
                if (binding.etWeight.text.isNullOrEmpty()) {
                    binding.etWeight.setText(weight.weight.toString())
                }
            } else {
                // 今天没有记录
                binding.tvTodayWeight.text = getString(R.string.no_weight_today)
                binding.btnSave.text = getString(R.string.save_weight)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 