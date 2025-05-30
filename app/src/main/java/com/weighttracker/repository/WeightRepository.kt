package com.weighttracker.repository

import androidx.lifecycle.LiveData
import com.weighttracker.data.Weight
import com.weighttracker.data.WeightDao
import java.util.Date

class WeightRepository(private val weightDao: WeightDao) {

    fun getAllWeights(): LiveData<List<Weight>> = weightDao.getAllWeights()

    fun getWeightsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Weight>> =
        weightDao.getWeightsBetweenDates(startDate, endDate)

    fun getWeightByDate(date: Date): LiveData<Weight?> = weightDao.getWeightByDate(date)

    suspend fun getWeightByDateSync(date: Date): Weight? = weightDao.getWeightByDateSync(date)

    fun getRecentWeights(limit: Int): LiveData<List<Weight>> = weightDao.getRecentWeights(limit)

    suspend fun insertWeight(weight: Weight): Long = weightDao.insertWeight(weight)

    /**
     * 智能插入体重记录：每天只能有一个记录
     * 如果当天已有记录则更新，否则插入新记录
     */
    suspend fun insertOrUpdateDailyWeight(weight: Weight): Long = weightDao.insertOrUpdateDaily(weight)

    suspend fun updateWeight(weight: Weight) = weightDao.updateWeight(weight)

    suspend fun deleteWeight(weight: Weight) = weightDao.deleteWeight(weight)

    suspend fun deleteWeightById(id: Long) = weightDao.deleteWeightById(id)

    suspend fun deleteWeightByDate(date: Date) = weightDao.deleteWeightByDate(date)

    suspend fun getWeightCount(): Int = weightDao.getWeightCount()

    suspend fun getLatestWeight(): Weight? = weightDao.getLatestWeight()
} 