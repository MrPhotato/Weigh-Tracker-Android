package com.weighttracker.ui.chart

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.weighttracker.R
import com.weighttracker.data.Weight
import com.weighttracker.databinding.FragmentChartBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChartFragment : Fragment() {

    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChartViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()
        setupTimeRangeChips()
        observeViewModel()
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            animateX(1000)

            // 设置坐标轴
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = 5
                textSize = 12f
                textColor = ContextCompat.getColor(requireContext(), R.color.dark_gray)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                textSize = 12f
                textColor = ContextCompat.getColor(requireContext(), R.color.dark_gray)
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun setupTimeRangeChips() {
        binding.chip7days.setOnClickListener { viewModel.setTimeRange(7) }
        binding.chip30days.setOnClickListener { viewModel.setTimeRange(30) }
        binding.chip90days.setOnClickListener { viewModel.setTimeRange(90) }
        binding.chipAll.setOnClickListener { viewModel.setTimeRange(-1) }
    }

    private fun observeViewModel() {
        viewModel.chartData.observe(viewLifecycleOwner) { weights ->
            updateChart(weights)
        }
    }

    private fun updateChart(weights: List<Weight>) {
        if (weights.isEmpty()) {
            binding.lineChart.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            return
        }

        binding.lineChart.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE

        val entries = weights.mapIndexed { index, weight ->
            Entry(index.toFloat(), weight.weight)
        }

        val dataSet = LineDataSet(entries, "体重").apply {
            color = ContextCompat.getColor(requireContext(), R.color.chart_line)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.chart_line))
            lineWidth = 3f
            circleRadius = 6f
            setDrawCircleHole(false)
            valueTextSize = 12f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.dark_gray)
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.chart_line)
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData

        // 设置X轴标签
        binding.lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < weights.size) {
                    dateFormat.format(weights[index].date)
                } else {
                    ""
                }
            }
        }

        binding.lineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 