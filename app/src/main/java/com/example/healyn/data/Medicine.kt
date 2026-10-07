package com.example.healyn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val timeHour: Int,
    val timeMinute: Int,
    val numberOfDays: Int,
    val numberOfPills: Int,
    val remainingDays: Int = numberOfDays,
    val remainingPills: Int = numberOfPills,
    val isTaken: Boolean = false,
    val startDate: Long = System.currentTimeMillis()
)