package com.weighttracker.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.weighttracker.data.Weight
import com.weighttracker.data.WeightDatabase
import com.weighttracker.repository.WeightRepository
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeightRepository
    val allWeights: LiveData<List<Weight>>

    init {
        val weightDao = WeightDatabase.getDatabase(application).weightDao()
        repository = WeightRepository(weightDao)
        allWeights = repository.getAllWeights()
    }

    fun deleteWeight(weight: Weight) {
        viewModelScope.launch {
            repository.deleteWeight(weight)
        }
    }

    fun updateWeight(weight: Weight) {
        viewModelScope.launch {
            repository.updateWeight(weight)
        }
    }
} 