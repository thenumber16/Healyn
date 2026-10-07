package com.example.healyn

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.healyn.data.HealynDatabase
import com.example.healyn.notification.AlarmScheduler
import com.example.healyn.ui.theme.HealynTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.healyn.notification.NotificationHelper

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val stopRingingRunnable = Runnable { stopRinging() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        val medicineName = intent.getStringExtra("medicineName") ?: "Medicine"
        val medicineId = intent.getIntExtra("medicineId", 0)
        val timeStr = intent.getStringExtra("timeStr") ?: ""
        val remainingPills = intent.getIntExtra("remainingPills", 0)
        val remainingDays = intent.getIntExtra("remainingDays", 0)

        // Start ringing
        startRinging()

        // Stop ringing after 5 minutes automatically
        handler.postDelayed(stopRingingRunnable, 5 * 60 * 1000L)

        setContent {
            HealynTheme {
                AlarmScreen(
                    medicineName = medicineName,
                    medicineId = medicineId,
                    timeStr = timeStr,
                    remainingPills = remainingPills,
                    remainingDays = remainingDays,
                    onTaken = {
                        stopRinging()
                        markMedicineTaken(medicineId)
                        finish()
                    },
                    onSnooze = { minutes ->
                        stopRinging()
                        snoozeAlarm(medicineName, medicineId, minutes)
                        finish()
                    },
                    onDismiss = {
                        stopRinging()
                        finish()
                    }
                )
            }
        }
    }

    private fun startRinging() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmActivity, alarmUri)
                isLooping = true
                prepare()
                start()
            }

            // Max volume for alarm
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRinging() {
        handler.removeCallbacks(stopRingingRunnable)
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRinging()
    }

    private fun markMedicineTaken(medicineId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = HealynDatabase.getDatabase(applicationContext).healynDao()
            val medicines = dao.getAllMedicinesOnce()
            val medicine = medicines.firstOrNull { it.id == medicineId } ?: return@launch
            val newRemainingDays = medicine.remainingDays - 1
            val newRemainingPills = medicine.remainingPills - 1

            // Cancel the alarm notification
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
            notificationManager.cancel(medicineId)

            if (newRemainingDays <= 0 || newRemainingPills <= 0) {
                AlarmScheduler.cancelMedicineAlarm(applicationContext, medicineId)
                dao.deleteMedicine(medicine)
            } else {
                dao.updateMedicine(
                    medicine.copy(
                        isTaken = true,
                        remainingDays = newRemainingDays,
                        remainingPills = newRemainingPills
                    )
                )

                // Refill check — show notification instead of dialog
                if (newRemainingPills < newRemainingDays) {
                    showRefillNotification(medicine.name, newRemainingPills, newRemainingDays, medicineId)
                }
            }
        }
    }

    private fun showRefillNotification(
        medicineName: String,
        pills: Int,
        days: Int,
        medicineId: Int
    ) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            medicineId + 80000,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationHelper.MEDICINE_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ Refill Needed: $medicineName")
            .setContentText("Only $pills pills left but $days days remaining. Please refill soon!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(medicineId + 80000, notification)
    }

    private fun showRefillAlert(medicineName: String, pills: Int, days: Int) {
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Refill Needed!")
            .setMessage("You have $pills pills left but $days days remaining for $medicineName. Please refill soon!")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun snoozeAlarm(medicineName: String, medicineId: Int, minutes: Int) {
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        AlarmScheduler.scheduleSnoozeAlarm(
            applicationContext,
            medicineId,
            medicineName,
            triggerTime
        )
    }
}

@Composable
fun AlarmScreen(
    medicineName: String,
    medicineId: Int,
    timeStr: String,
    remainingPills: Int,
    remainingDays: Int,
    onTaken: () -> Unit,
    onSnooze: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var showCustomSnooze by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableStateOf("") }

    if (showCustomSnooze) {
        Dialog(onDismissRequest = { showCustomSnooze = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Custom Snooze",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { customMinutes = it },
                        placeholder = { Text("Enter minutes (1-240)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCustomSnooze = false }) {
                            Text("Cancel", color = Color(0xFF5B6E8C))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val mins = customMinutes.toIntOrNull()
                                if (mins != null && mins in 1..240) {
                                    onSnooze(mins)
                                    showCustomSnooze = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5)
                            ),
                            shape = RoundedCornerShape(40.dp)
                        ) {
                            Text("Snooze", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(48.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column {
                // Purple gradient header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                            ),
                            shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)
                        )
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "💊", fontSize = 36.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = timeStr,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Medicine reminder · due now",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = medicineName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFE), RoundedCornerShape(16.dp))
                    ) {
                        Row {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(48.dp)
                                    .background(
                                        Color(0xFF4F46E5),
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            bottomStart = 16.dp
                                        )
                                    )
                            )
                            Text(
                                text = "ℹ️ Take as prescribed by your doctor.",
                                fontSize = 14.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "💊 $remainingPills pills left",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "📅 $remainingDays days remaining",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onTaken,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Text(
                            text = "✅ Yes, I took it",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "⏰ 15 min" to 15,
                            "🍽️ 30 min" to 30,
                            "🚗 1 hour" to 60
                        ).forEach { (label, minutes) ->
                            Button(
                                onClick = { onSnooze(minutes) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF1F5F9)
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showCustomSnooze = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(60.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp, Color(0xFFE2E8F0)
                        )
                    ) {
                        Text(
                            text = "🕐 Custom snooze",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
        }
    }
}