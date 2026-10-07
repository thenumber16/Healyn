package com.example.healyn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val doctorName: String,
    val department: String,
    val dateTimeMillis: Long,
    val remindDaysBefore: Int = 1
)