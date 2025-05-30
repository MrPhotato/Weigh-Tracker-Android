package com.weighttracker.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.Date

@Dao
interface WeightDao {
    @Query("SELECT * FROM weights ORDER BY date DESC")
    fun getAllWeights(): LiveData<List<Weight>>

    @Query("SELECT * FROM weights WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWeightsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Weight>>

    @Query("SELECT * FROM weights WHERE date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    fun getWeightByDate(date: Date): LiveData<Weight?>

    @Query("SELECT * FROM weights WHERE date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getWeightByDateSync(date: Date): Weight?

    @Query("SELECT * FROM weights ORDER BY date DESC LIMIT :limit")
    fun getRecentWeights(limit: Int): LiveData<List<Weight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: Weight): Long

    @Update
    suspend fun updateWeight(weight: Weight)

    @Delete
    suspend fun deleteWeight(weight: Weight)

    @Query("DELETE FROM weights WHERE id = :id")
    suspend fun deleteWeightById(id: Long)

    @Query("DELETE FROM weights WHERE date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun deleteWeightByDate(date: Date)

    @Query("SELECT COUNT(*) FROM weights")
    suspend fun getWeightCount(): Int

    @Query("SELECT * FROM weights ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWeight(): Weight?

    /**
     * 插入或更新当天的体重记录
     * 如果当天已有记录，则更新；否则插入新记录
     */
    @Transaction
    suspend fun insertOrUpdateDaily(weight: Weight): Long {
        val existingWeight = getWeightByDateSync(weight.date)
        return if (existingWeight != null) {
            // 保持原有的ID，更新其他字段
            val updatedWeight = weight.copy(id = existingWeight.id)
            updateWeight(updatedWeight)
            existingWeight.id
        } else {
            // 插入新记录
            insertWeight(weight)
        }
    }
} 