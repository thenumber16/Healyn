package com.example.healyn.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healyn.data.Appointment
import com.example.healyn.data.HealynDatabase
import com.example.healyn.data.Medicine
import com.example.healyn.data.SymptomLog
import com.example.healyn.notification.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealynViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = HealynDatabase.getDatabase(application).healynDao()
    private val context = application.applicationContext

    val medicines = dao.getAllMedicines().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val appointments = dao.getAllAppointments().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentSymptomLogs = dao.getRecentSymptomLogs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _refillAlertMedicine = MutableStateFlow<Medicine?>(null)
    val refillAlertMedicine: StateFlow<Medicine?> = _refillAlertMedicine

    fun dismissRefillAlert() {
        _refillAlertMedicine.value = null
    }

    fun addMedicine(medicine: Medicine) {
        viewModelScope.launch {
            dao.insertMedicine(medicine)
            val allMedicines = dao.getAllMedicinesOnce()
            val inserted = allMedicines.lastOrNull { it.name == medicine.name }
            inserted?.let { AlarmScheduler.scheduleMedicineAlarm(context, it) }
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            AlarmScheduler.cancelMedicineAlarm(context, medicine.id)
            dao.deleteMedicine(medicine)
        }
    }

    fun markMedicineTaken(medicine: Medicine) {
        viewModelScope.launch {
            val newRemainingDays = medicine.remainingDays - 1
            val newRemainingPills = medicine.remainingPills - 1

            if (newRemainingDays <= 0 || newRemainingPills <= 0) {
                AlarmScheduler.cancelMedicineAlarm(context, medicine.id)
                dao.deleteMedicine(medicine)
                return@launch
            }

            val updated = medicine.copy(
                isTaken = true,
                remainingDays = newRemainingDays,
                remainingPills = newRemainingPills
            )
            dao.updateMedicine(updated)

            if (newRemainingPills < newRemainingDays) {
                _refillAlertMedicine.value = updated
            }
        }
    }

    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            dao.insertAppointment(appointment)
            val allAppointments = dao.getAllAppointmentsOnce()
            val inserted = allAppointments.lastOrNull { it.doctorName == appointment.doctorName }
            inserted?.let { AlarmScheduler.scheduleAppointmentAlarm(context, it) }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            AlarmScheduler.cancelAppointmentAlarm(context, appointment.id)
            dao.deleteAppointment(appointment)
        }
    }

    fun logSymptom(symptom: String) {
        viewModelScope.launch {
            dao.insertSymptomLog(SymptomLog(symptom = symptom))
        }
    }
}