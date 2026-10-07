package com.example.healyn.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.healyn.data.Medicine
import com.example.healyn.data.SymptomLog
import com.example.healyn.viewmodel.HealynViewModel
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

val PrimaryTeal = Color(0xFF0D9488)
val BackgroundBlue = Color(0xFFF0FAFA)
val CardWhite = Color.White
val TextDark = Color(0xFF0F172A)
val TextSecondary = Color(0xFF5B6E8C)
val GreenLight = Color(0xFFE6F7E6)
val GreenText = Color(0xFF2B7E3A)
val DoctorColor = Color(0xFF9BF765)

private val DOCTOR_NAME_KEY = stringPreferencesKey("doctor_name")
private val DOCTOR_PHONE_KEY = stringPreferencesKey("doctor_phone")
private val CAREGIVER_NAME_KEY = stringPreferencesKey("caregiver_name")
private val CAREGIVER_PHONE_KEY = stringPreferencesKey("caregiver_phone")

@Composable
fun HomeScreen(viewModel: HealynViewModel) {
    val medicines by viewModel.medicines.collectAsState()
    val refillAlert by viewModel.refillAlertMedicine.collectAsState()
    val recentLogs by viewModel.recentSymptomLogs.collectAsState()

    val takenCount = medicines.count { it.isTaken }
    val totalCount = medicines.size

    refillAlert?.let { medicine ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissRefillAlert() },
            title = {
                Text(
                    text = "⚠️ Refill Needed!",
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "You have ${medicine.remainingPills} pills left but ${medicine.remainingDays} days remaining for ${medicine.name}. Please refill soon!",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissRefillAlert() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = CardWhite
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlue)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            QuickCallCard()
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 Today's schedule",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GreenLight)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$takenCount/$totalCount taken",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GreenText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (medicines.isEmpty()) {
                        Text(
                            text = "No medicines added yet.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        medicines.forEach { medicine ->
                            MedicineItem(
                                medicine = medicine,
                                onMarkTaken = { viewModel.markMedicineTaken(medicine) },
                                onDelete = { viewModel.deleteMedicine(medicine) }
                            )
                            if (medicine != medicines.last()) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏥 How are you feeling?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SymptomButton(
                            emoji = "😵",
                            label = "Dizzy",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.logSymptom("Dizzy") }
                        )
                        SymptomButton(
                            emoji = "🤢",
                            label = "Nausea",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.logSymptom("Nausea") }
                        )
                        SymptomButton(
                            emoji = "🤕",
                            label = "Headache",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.logSymptom("Headache") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SymptomButton(
                            emoji = "🤒",
                            label = "Fever",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.logSymptom("Fever") }
                        )
                        SymptomButton(
                            emoji = "😴",
                            label = "Fatigue",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.logSymptom("Fatigue") }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (recentLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "📋 Recent logs",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        recentLogs.forEach { log ->
                            SymptomLogItem(log = log)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 Adherence streak",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Text(
                            text = "14 days 🔥",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { 0.75f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        color = PrimaryTeal,
                        trackColor = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🎯 92% adherence this month",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun QuickCallCard() {
    val context = LocalContext.current

    var doctorName by remember { mutableStateOf("") }
    var doctorPhone by remember { mutableStateOf("") }
    var caregiverName by remember { mutableStateOf("") }
    var caregiverPhone by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        doctorName = prefs[DOCTOR_NAME_KEY] ?: ""
        doctorPhone = prefs[DOCTOR_PHONE_KEY] ?: ""
        caregiverName = prefs[CAREGIVER_NAME_KEY] ?: ""
        caregiverPhone = prefs[CAREGIVER_PHONE_KEY] ?: ""
    }

    if (doctorPhone.isEmpty() && caregiverPhone.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📞 Quick Call",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (doctorPhone.isNotEmpty()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL,
                                Uri.parse("tel:$doctorPhone"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DoctorColor
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👨‍⚕️", fontSize = 18.sp)
                            Text(
                                text = if (doctorName.isNotEmpty()) doctorName else "Doctor",
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                if (caregiverPhone.isNotEmpty()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL,
                                Uri.parse("tel:$caregiverPhone"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF80DEEA)
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👨‍👩‍👧", fontSize = 18.sp)
                            Text(
                                text = if (caregiverName.isNotEmpty()) caregiverName else "Caregiver",
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SymptomButton(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var clicked by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(60.dp))
            .background(if (clicked) Color(0xFFEEF2FF) else Color.White)
            .clickable {
                clicked = true
                onClick()
            }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SymptomLogItem(log: SymptomLog) {
    val emoji = when (log.symptom) {
        "Dizzy" -> "😵"
        "Nausea" -> "🤢"
        "Headache" -> "🤕"
        "Fever" -> "🤒"
        "Fatigue" -> "😴"
        else -> "🏥"
    }
    val dateStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        .format(Date(log.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$emoji ${log.symptom}",
            fontSize = 13.sp,
            color = TextDark
        )
        Text(
            text = dateStr,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun MedicineItem(
    medicine: Medicine,
    onMarkTaken: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Delete Medicine?",
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${medicine.name}?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = CardWhite
        )
    }

    val timeStr = String.format(
        "%02d:%02d %s",
        if (medicine.timeHour % 12 == 0) 12 else medicine.timeHour % 12,
        medicine.timeMinute,
        if (medicine.timeHour < 12) "AM" else "PM"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = medicine.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "$timeStr · ${medicine.remainingDays} days · ${medicine.remainingPills} pills left",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (medicine.isTaken) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(40.dp))
                        .background(GreenLight)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Taken ✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenText
                    )
                }
            } else {
                Button(
                    onClick = onMarkTaken,
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "✔ Mark taken",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(32.dp)
            ) {
                Text(text = "🗑️", fontSize = 16.sp)
            }
        }
    }
}