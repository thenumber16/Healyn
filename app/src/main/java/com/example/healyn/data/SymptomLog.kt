package com.example.healyn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom_logs")
data class SymptomLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val symptom: String,
    val timestamp: Long = System.currentTimeMillis()
)