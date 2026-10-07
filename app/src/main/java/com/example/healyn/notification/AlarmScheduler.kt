package com.example.healyn.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.healyn.data.Appointment
import com.example.healyn.data.Medicine
import java.util.Calendar

object AlarmScheduler {

    fun scheduleMedicineAlarm(context: Context, medicine: Medicine) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val timeStr = String.format(
            "%02d:%02d %s",
            if (medicine.timeHour % 12 == 0) 12 else medicine.timeHour % 12,
            medicine.timeMinute,
            if (medicine.timeHour < 12) "AM" else "PM"
        )

        val broadcastIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("type", "medicine")
            putExtra("medicineName", medicine.name)
            putExtra("medicineId", medicine.id)
            putExtra("timeStr", timeStr)
            putExtra("remainingPills", medicine.remainingPills)
            putExtra("remainingDays", medicine.remainingDays)
            putExtra("notificationId", medicine.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicine.id,
            broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, medicine.timeHour)
            set(Calendar.MINUTE, medicine.timeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun scheduleSnoozeAlarm(
        context: Context,
        medicineId: Int,
        medicineName: String,
        triggerTimeMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val broadcastIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("type", "medicine")
            putExtra("medicineName", medicineName)
            putExtra("medicineId", medicineId)
            putExtra("notificationId", medicineId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId + 50000,
            broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    fun cancelMedicineAlarm(context: Context, medicineId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleAppointmentAlarm(context: Context, appointment: Appointment) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("type", "appointment")
            putExtra("doctorName", appointment.doctorName)
            putExtra("department", appointment.department)
            putExtra("notificationId", appointment.id + 10000)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = appointment.dateTimeMillis
            add(Calendar.DAY_OF_YEAR, -appointment.remindDaysBefore)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelAppointmentAlarm(context: Context, appointmentId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}