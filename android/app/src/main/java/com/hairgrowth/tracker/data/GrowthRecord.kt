package com.hairgrowth.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "growth_records")
data class GrowthRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,         // LocalDate.toEpochDay()
    val measuredLengthMm: Float,
    val note: String = ""
)
