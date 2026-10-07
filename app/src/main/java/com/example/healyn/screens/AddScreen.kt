package com.example.healyn.screens

import android.app.TimePickerDialog
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.healyn.data.Appointment
import com.example.healyn.data.Medicine
import com.example.healyn.viewmodel.HealynViewModel
import java.util.Calendar

@Composable
fun AddScreen(viewModel: HealynViewModel) {
    var showMedDialog by remember { mutableStateOf(false) }
    var showApptDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlue)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "➕ Add new",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Add Medicine Button
                Button(
                    onClick = { showMedDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text(
                        text = "💊 Add Medicine",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Appointment Button
                OutlinedButton(
                    onClick = { showApptDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(60.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryTeal)
                ) {
                    Text(
                        text = "📅 Add Doctor's Appointment",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Smart features box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundBlue, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "💡 Smart features ready:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "📆 Duration & auto-delete after tenure", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "📦 Stock tracking & refill alerts", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "⏰ Snooze with flexible options", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        }
    }

    if (showMedDialog) {
        AddMedicineDialog(
            onDismiss = { showMedDialog = false },
            onConfirm = { medicine ->
                viewModel.addMedicine(medicine)
                showMedDialog = false
            }
        )
    }

    if (showApptDialog) {
        AddAppointmentDialog(
            onDismiss = { showApptDialog = false },
            onConfirm = { appointment ->
                viewModel.addAppointment(appointment)
                showApptDialog = false
            }
        )
    }
}

@Composable
fun AddMedicineDialog(onDismiss: () -> Unit, onConfirm: (Medicine) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var pills by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }
    var timeDisplay by remember { mutableStateOf("Tap to set time") }

    val timePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedHour = hour
            selectedMinute = minute
            timeDisplay = String.format(
                "%02d:%02d %s",
                if (hour % 12 == 0) 12 else hour % 12,
                minute,
                if (hour < 12) "AM" else "PM"
            )
        },
        selectedHour, selectedMinute, false
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "💊 Add Medicine",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Medicine name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time picker button
                OutlinedButton(
                    onClick = { timePicker.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text(text = timeDisplay, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = days,
                        onValueChange = { days = it },
                        placeholder = { Text("Number of days") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pills,
                        onValueChange = { pills = it },
                        placeholder = { Text("Number of pills") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && days.isNotBlank() && pills.isNotBlank()) {
                                onConfirm(
                                    Medicine(
                                        name = name,
                                        timeHour = selectedHour,
                                        timeMinute = selectedMinute,
                                        numberOfDays = days.toIntOrNull() ?: 1,
                                        numberOfPills = pills.toIntOrNull() ?: 1
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("Add", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddAppointmentDialog(onDismiss: () -> Unit, onConfirm: (Appointment) -> Unit) {
    val context = LocalContext.current
    var doctorName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var dateDisplay by remember { mutableStateOf("Tap to set date") }

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            selectedDateMillis = calendar.timeInMillis
            dateDisplay = "$day/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "👨‍⚕️ Add Appointment",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    placeholder = { Text("Doctor's name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    placeholder = { Text("Department (e.g., Cardiology)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date picker button
                OutlinedButton(
                    onClick = { datePicker.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text(text = dateDisplay, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (doctorName.isNotBlank() && department.isNotBlank()) {
                                onConfirm(
                                    Appointment(
                                        doctorName = doctorName,
                                        department = department,
                                        dateTimeMillis = selectedDateMillis
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("Add", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}