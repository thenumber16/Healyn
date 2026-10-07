package com.example.healyn.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.healyn.data.HealynDatabase
import com.example.healyn.notification.AlarmScheduler

class MidnightResetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val dao = HealynDatabase.getDatabase(applicationContext).healynDao()

            // Reset all medicines to not taken
            dao.resetAllMedicineTaken()

            // Auto delete expired medicines and cancel their alarms
            val medicines = dao.getAllMedicinesOnce()
            medicines.forEach { medicine ->
                if (medicine.remainingDays <= 0 || medicine.remainingPills <= 0) {
                    AlarmScheduler.cancelMedicineAlarm(applicationContext, medicine.id)
                    dao.deleteMedicine(medicine)
                }
            }

            // Auto delete past appointments
            val now = System.currentTimeMillis()
            val appointments = dao.getAllAppointmentsOnce()
            appointments.forEach { appointment ->
                if (appointment.dateTimeMillis < now) {
                    AlarmScheduler.cancelAppointmentAlarm(applicationContext, appointment.id)
                    dao.deleteAppointment(appointment)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}