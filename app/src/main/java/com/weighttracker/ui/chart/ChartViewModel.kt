package com.weighttracker.ui.chart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.weighttracker.data.Weight
import com.weighttracker.data.WeightDatabase
import com.weighttracker.repository.WeightRepository
import java.util.Calendar
import java.util.Date

class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeightRepository
    private val _timeRangeDays = MutableLiveData<Int>(7)
    private val _chartData = MediatorLiveData<List<Weight>>()
    
    val chartData: LiveData<List<Weight>> = _chartData
    val timeRangeDays: LiveData<Int> = _timeRangeDays

    init {
        val weightDao = WeightDatabase.getDatabase(application).weightDao()
        repository = WeightRepository(weightDao)
        
        setupChartData()
    }

    private fun setupChartData() {
        _chartData.addSource(_timeRangeDays) { days ->
            updateChartData(days)
        }
    }

    private fun updateChartData(days: Int) {
        if (days == -1) {
            // 显示全部数据
            _chartData.addSource(repository.getAllWeights()) { weights ->
                _chartData.value = weights.reversed() // 按时间正序排列
            }
        } else {
            // 显示指定天数的数据
            val endDate = Date()
            val startDate = Calendar.getInstance().apply {
                time = endDate
                add(Calendar.DAY_OF_YEAR, -days)
            }.time
            
            _chartData.addSource(repository.getWeightsBetweenDates(startDate, endDate)) { weights ->
                _chartData.value = weights
            }
        }
    }

    fun setTimeRange(days: Int) {
        if (_timeRangeDays.value != days) {
            // 清除之前的数据源
            _chartData.removeSource(repository.getAllWeights())
            
            _timeRangeDays.value = days
        }
    }
} 