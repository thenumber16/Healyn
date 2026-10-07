package com.example.healyn.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.healyn.R
import android.app.PendingIntent
import android.content.Intent

object NotificationHelper {

    const val MEDICINE_CHANNEL_ID = "medicine_reminder_channel"
    const val APPOINTMENT_CHANNEL_ID = "appointment_reminder_channel"

    fun createNotificationChannels(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val medicineChannel = NotificationChannel(
            MEDICINE_CHANNEL_ID,
            "Medicine Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for medicine reminders"
            setSound(alarmSound, audioAttributes)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
        }

        val appointmentChannel = NotificationChannel(
            APPOINTMENT_CHANNEL_ID,
            "Appointment Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for doctor appointments"
            setSound(alarmSound, audioAttributes)
            enableVibration(true)
        }

        notificationManager.createNotificationChannel(medicineChannel)
        notificationManager.createNotificationChannel(appointmentChannel)
    }

    fun showMedicineNotification(
        context: Context,
        medicineName: String,
        notificationId: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Big text style for phones
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("Time to take $medicineName. Tap to open Healyn.")
            .setBigContentTitle("💊 Medicine Reminder")
            .setSummaryText("Healyn")

        // Wearable extender for smartwatch — compact and clean
        val wearableExtender = NotificationCompat.WearableExtender()
            .setHintShowBackgroundOnly(true)

        val notification = NotificationCompat.Builder(context, MEDICINE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 $medicineName")
            .setContentText("Time to take your medicine")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(null, true)
            .setLocalOnly(false)
            .extend(wearableExtender)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showAppointmentNotification(
        context: Context,
        doctorName: String,
        department: String,
        notificationId: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("Dr. $doctorName ($department) is tomorrow. Open Healyn to view details.")
            .setBigContentTitle("🏥 Appointment Tomorrow")
            .setSummaryText("Healyn")

        val wearableExtender = NotificationCompat.WearableExtender()
            .setHintShowBackgroundOnly(true)

        val notification = NotificationCompat.Builder(context, APPOINTMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🏥 Dr. $doctorName")
            .setContentText("Appointment tomorrow · $department")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .setLocalOnly(false)
            .extend(wearableExtender)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showMissedDoseNotification(
        context: Context,
        medicineName: String,
        notificationId: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open MainActivity when notification is tapped
        val openAppIntent = Intent(context, Class.forName("com.example.healyn.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val wearableExtender = NotificationCompat.WearableExtender()
            .setHintShowBackgroundOnly(true)

        val notification = NotificationCompat.Builder(context, MEDICINE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ Missed: $medicineName")
            .setContentText("You missed your dose. Tap to open Healyn.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setLocalOnly(false)
            .extend(wearableExtender)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}