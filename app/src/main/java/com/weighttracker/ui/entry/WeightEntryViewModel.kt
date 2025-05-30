package com.weighttracker.ui.entry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.weighttracker.data.Weight
import com.weighttracker.data.WeightDatabase
import com.weighttracker.repository.WeightRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class WeightEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeightRepository
    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    val todayWeight: LiveData<Weight?>

    init {
        val weightDao = WeightDatabase.getDatabase(application).weightDao()
        repository = WeightRepository(weightDao)
        
        // 获取今天的体重记录
        val today = getTodayDate()
        todayWeight = repository.getWeightByDate(today)
    }

    /**
     * 保存体重记录 - 每天只能有一个记录
     * 如果今天已有记录则更新，否则插入新记录
     */
    fun saveWeight(weightValue: Float, notes: String = "") {
        viewModelScope.launch {
            try {
                val weight = Weight(
                    weight = weightValue,
                    date = getTodayDate(), // 使用标准化的今天日期
                    notes = notes.ifBlank { null }
                )
                // 使用智能插入方法：今天有记录则更新，否则插入
                repository.insertOrUpdateDailyWeight(weight)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }

    /**
     * 获取标准化的今天日期（只包含年月日，不包含时分秒）
     */
    private fun getTodayDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * 获取今天是否已有体重记录
     */
    fun hasTodayWeight(): Boolean {
        return todayWeight.value != null
    }
} 