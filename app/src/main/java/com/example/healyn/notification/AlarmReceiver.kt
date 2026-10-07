package com.example.healyn.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.healyn.AlarmActivity
import com.example.healyn.R
import com.example.healyn.data.HealynDatabase
import com.example.healyn.worker.MissedDoseWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: return
        val notificationId = intent.getIntExtra("notificationId", 0)

        when (type) {
            "medicine" -> {
                val medicineName = intent.getStringExtra("medicineName") ?: return
                val medicineId = intent.getIntExtra("medicineId", 0)
                val timeStr = intent.getStringExtra("timeStr") ?: ""
                val remainingPills = intent.getIntExtra("remainingPills", 0)
                val remainingDays = intent.getIntExtra("remainingDays", 0)

                val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
                    putExtra("medicineName", medicineName)
                    putExtra("medicineId", medicineId)
                    putExtra("timeStr", timeStr)
                    putExtra("remainingPills", remainingPills)
                    putExtra("remainingDays", remainingDays)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

                val fullScreenPendingIntent = PendingIntent.getActivity(
                    context,
                    medicineId,
                    fullScreenIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val powerManager =
                    context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val isScreenOn = powerManager.isInteractive

                if (isScreenOn) {
                    val notification = NotificationCompat.Builder(
                        context,
                        NotificationHelper.MEDICINE_CHANNEL_ID
                    )
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("💊 Time to take $medicineName!")
                        .setContentText("Tap to open Healyn")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(fullScreenPendingIntent)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(notificationId, notification)
                } else {
                    val notification = NotificationCompat.Builder(
                        context,
                        NotificationHelper.MEDICINE_CHANNEL_ID
                    )
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("💊 Time to take $medicineName!")
                        .setContentText("Tap to open Healyn")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setFullScreenIntent(fullScreenPendingIntent, true)
                        .setContentIntent(fullScreenPendingIntent)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(notificationId, notification)
                    context.startActivity(fullScreenIntent)
                }

                // Reschedule for next day using fresh DB values
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = HealynDatabase.getDatabase(context).healynDao()
                    val medicines = dao.getAllMedicinesOnce()
                    val medicine = medicines.firstOrNull { it.id == medicineId }

                    if (medicine != null) {
                        val alarmManager =
                            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                        val nextDayBroadcastIntent =
                            Intent(context, AlarmReceiver::class.java).apply {
                                putExtra("type", "medicine")
                                putExtra("medicineName", medicine.name)
                                putExtra("medicineId", medicine.id)
                                putExtra("notificationId", medicine.id)
                                putExtra("remainingPills", medicine.remainingPills)
                                putExtra("remainingDays", medicine.remainingDays)
                                val nextTimeStr = String.format(
                                    "%02d:%02d %s",
                                    if (medicine.timeHour % 12 == 0) 12
                                    else medicine.timeHour % 12,
                                    medicine.timeMinute,
                                    if (medicine.timeHour < 12) "AM" else "PM"
                                )
                                putExtra("timeStr", nextTimeStr)
                            }

                        val nextDayPendingIntent = PendingIntent.getBroadcast(
                            context,
                            medicine.id,
                            nextDayBroadcastIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        val nextDayCalendar = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, 1)
                            set(Calendar.HOUR_OF_DAY, medicine.timeHour)
                            set(Calendar.MINUTE, medicine.timeMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (android.os.Build.VERSION.SDK_INT >=
                            android.os.Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    nextDayCalendar.timeInMillis,
                                    nextDayPendingIntent
                                )
                            }
                        } else {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                nextDayCalendar.timeInMillis,
                                nextDayPendingIntent
                            )
                        }
                    }
                }

                // Schedule missed dose check after 30 minutes
                val missedDoseData = Data.Builder()
                    .putInt("medicineId", medicineId)
                    .putString("medicineName", medicineName)
                    .putString("timeStr", timeStr)
                    .build()

                val missedDoseWork = OneTimeWorkRequestBuilder<MissedDoseWorker>()
                    .setInitialDelay(30, TimeUnit.MINUTES)
                    .setInputData(missedDoseData)
                    .build()

                WorkManager.getInstance(context).enqueue(missedDoseWork)
            }

            "appointment" -> {
                val doctorName = intent.getStringExtra("doctorName") ?: return
                val department = intent.getStringExtra("department") ?: return
                NotificationHelper.showAppointmentNotification(
                    context, doctorName, department, notificationId
                )
            }
        }
    }
}