package com.weighttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "weights",
    indices = [Index(value = ["date"], unique = true)]
)
data class Weight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Float,
    val date: Date,
    val notes: String? = null
) 