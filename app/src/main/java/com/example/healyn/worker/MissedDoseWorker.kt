package com.example.healyn.worker

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.healyn.data.HealynDatabase
import com.example.healyn.notification.NotificationHelper
import com.example.healyn.screens.dataStore
import kotlinx.coroutines.flow.first

class MissedDoseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val medicineId = inputData.getInt("medicineId", -1)
        val medicineName = inputData.getString("medicineName") ?: return Result.success()
        val timeStr = inputData.getString("timeStr") ?: ""

        if (medicineId == -1) return Result.success()

        val dao = HealynDatabase.getDatabase(applicationContext).healynDao()
        val medicines = dao.getAllMedicinesOnce()
        val medicine = medicines.firstOrNull { it.id == medicineId }

        // If medicine is already taken or deleted, do nothing
        if (medicine == null || medicine.isTaken) return Result.success()

        // Show missed dose notification via NotificationHelper
        NotificationHelper.showMissedDoseNotification(
            applicationContext,
            medicineName,
            medicineId + 99000
        )

        // Get caregiver phone from DataStore
        val prefs = applicationContext.dataStore.data.first()
        val caregiverPhone = prefs[stringPreferencesKey("caregiver_phone")] ?: ""

        if (caregiverPhone.isNotEmpty()) {
            sendSmsToCaregiver(caregiverPhone, medicineName, timeStr)
        }

        return Result.success()
    }

    private fun sendSmsToCaregiver(
        caregiverPhone: String,
        medicineName: String,
        timeStr: String
    ) {
        try {
            val message = "Healyn Alert: Medicine '$medicineName' scheduled at $timeStr has not been taken. Please check on the patient."

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val smsManager =
                    applicationContext.getSystemService(SmsManager::class.java)
                smsManager?.sendTextMessage(caregiverPhone, null, message, null, null)
            } else {
                @Suppress("DEPRECATION")
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(caregiverPhone, null, message, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}