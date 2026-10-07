package com.example.healyn.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.healyn.data.HealynDatabase
import com.example.healyn.pdf.PdfReportGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment

internal val Context.dataStore by preferencesDataStore(name = "healyn_settings")

private val DOCTOR_NAME = stringPreferencesKey("doctor_name")
private val DOCTOR_PHONE = stringPreferencesKey("doctor_phone")
private val CAREGIVER_NAME = stringPreferencesKey("caregiver_name")
private val CAREGIVER_PHONE = stringPreferencesKey("caregiver_phone")
private val PATIENT_NAME = stringPreferencesKey("patient_name")
private val PATIENT_AGE = stringPreferencesKey("patient_age")
private val PATIENT_SEX = stringPreferencesKey("patient_sex")

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var doctorName by remember { mutableStateOf("") }
    var doctorPhone by remember { mutableStateOf("") }
    var caregiverName by remember { mutableStateOf("") }
    var caregiverPhone by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var generatingPdf by remember { mutableStateOf(false) }
    var patientName by remember { mutableStateOf("") }
    var patientAge by remember { mutableStateOf("") }
    var patientSex by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        doctorName = prefs[DOCTOR_NAME] ?: ""
        doctorPhone = prefs[DOCTOR_PHONE] ?: ""
        caregiverName = prefs[CAREGIVER_NAME] ?: ""
        caregiverPhone = prefs[CAREGIVER_PHONE] ?: ""
        patientName = prefs[PATIENT_NAME] ?: ""
        patientAge = prefs[PATIENT_AGE] ?: ""
        patientSex = prefs[PATIENT_SEX] ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlue),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // White top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "⚙️ Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7FD0C8),
                modifier = Modifier.align(Alignment.CenterStart)
            )
            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("Back", color = Color(0xFF0D9488),fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Patient Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👤 Patient Info",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        placeholder = { Text("Patient name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = patientAge,
                        onValueChange = { patientAge = it },
                        placeholder = { Text("Age") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sex",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female", "Other").forEach { option ->
                            Button(
                                onClick = { patientSex = option },
                                shape = RoundedCornerShape(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (patientSex == option)
                                        Color(0xFF0D9488) else Color(0xFFF1F5F9)
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 13.sp,
                                    color = if (patientSex == option) Color.White else TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Doctor card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👨‍⚕️ Doctor",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        placeholder = { Text("Doctor's name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = doctorPhone,
                        onValueChange = { doctorPhone = it },
                        placeholder = { Text("Doctor's phone number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            }

            // Caregiver card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👨‍👩‍👧 Caregiver",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = caregiverName,
                        onValueChange = { caregiverName = it },
                        placeholder = { Text("Caregiver's name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = caregiverPhone,
                        onValueChange = { caregiverPhone = it },
                        placeholder = { Text("Caregiver's phone number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            }

            // Save button
            Button(
                onClick = {
                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[DOCTOR_NAME] = doctorName
                            prefs[DOCTOR_PHONE] = doctorPhone
                            prefs[CAREGIVER_NAME] = caregiverName
                            prefs[CAREGIVER_PHONE] = caregiverPhone
                            prefs[PATIENT_NAME] = patientName
                            prefs[PATIENT_AGE] = patientAge
                            prefs[PATIENT_SEX] = patientSex
                        }
                        saved = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text(
                    text = if (saved) "✅ Saved!" else "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // PDF Report card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📄 Health Report",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Generate a monthly PDF report with your medicines, appointments and symptoms. Share it with your doctor.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                generatingPdf = true
                                val dao = HealynDatabase.getDatabase(context).healynDao()
                                val medicines = dao.getAllMedicinesOnce()
                                val appointments = dao.getAllAppointmentsOnce()
                                val prefs = context.dataStore.data.first()
                                val dName = prefs[DOCTOR_NAME] ?: ""
                                val cName = prefs[CAREGIVER_NAME] ?: ""
                                val allLogs = dao.getAllSymptomLogsOnce()
                                PdfReportGenerator.generateAndShare(
                                    context = context,
                                    medicines = medicines,
                                    appointments = appointments,
                                    symptomLogs = allLogs,
                                    doctorName = dName,
                                    caregiverName = cName,
                                    patientName = prefs[PATIENT_NAME] ?: "",
                                    patientAge = prefs[PATIENT_AGE] ?: "",
                                    patientSex = prefs[PATIENT_SEX] ?: ""
                                )
                                generatingPdf = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Text(
                            text = if (generatingPdf) "Generating..." else "📄 Generate & Share PDF",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}