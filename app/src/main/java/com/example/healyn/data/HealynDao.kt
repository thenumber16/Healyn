package com.example.healyn.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealynDao {

    // Medicine queries
    @Insert
    suspend fun insertMedicine(medicine: Medicine)

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("SELECT * FROM medicines ORDER BY timeHour, timeMinute")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines ORDER BY timeHour, timeMinute")
    suspend fun getAllMedicinesOnce(): List<Medicine>

    @Query("UPDATE medicines SET isTaken = :taken WHERE id = :id")
    suspend fun updateMedicineTaken(id: Int, taken: Boolean)
    @Query("UPDATE medicines SET isTaken = 0")
    suspend fun resetAllMedicineTaken()

    // Appointment queries
    @Insert
    suspend fun insertAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    @Query("SELECT * FROM appointments ORDER BY dateTimeMillis ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments ORDER BY dateTimeMillis ASC")
    suspend fun getAllAppointmentsOnce(): List<Appointment>

    // Symptom log queries
    @Insert
    suspend fun insertSymptomLog(symptomLog: SymptomLog)

    @Query("SELECT * FROM symptom_logs ORDER BY timestamp DESC LIMIT 3")
    fun getRecentSymptomLogs(): Flow<List<SymptomLog>>

    @Query("SELECT * FROM symptom_logs ORDER BY timestamp DESC")
    suspend fun getAllSymptomLogsOnce(): List<SymptomLog>
}