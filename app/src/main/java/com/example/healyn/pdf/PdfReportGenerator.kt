package com.example.healyn.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.healyn.data.Appointment
import com.example.healyn.data.Medicine
import com.example.healyn.data.SymptomLog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    fun generateAndShare(
        context: Context,
        medicines: List<Medicine>,
        appointments: List<Appointment>,
        symptomLogs: List<SymptomLog>,
        doctorName: String,
        caregiverName: String,
        patientName: String = "",
        patientAge: String = "",
        patientSex: String = ""
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(79, 70, 229)
            textSize = 24f
            isFakeBoldText = true
        }

        val headingPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 16f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(91, 110, 140)
            textSize = 13f
        }

        val linePaint = Paint().apply {
            color = Color.rgb(229, 231, 235)
            strokeWidth = 1f
        }

        var y = 60f

        // Header
        canvas.drawText("Healyn Health Report", 40f, y, titlePaint)
        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        val dateStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("Month: $dateStr", 40f, y, bodyPaint)
        y += 20f

        if (patientName.isNotEmpty()) {
            canvas.drawText("Patient: $patientName", 40f, y, bodyPaint)
            y += 20f
        }
        if (patientAge.isNotEmpty()) {
            canvas.drawText("Age: $patientAge", 40f, y, bodyPaint)
            y += 20f
        }
        if (patientSex.isNotEmpty()) {
            canvas.drawText("Sex: $patientSex", 40f, y, bodyPaint)
            y += 20f
        }
        if (doctorName.isNotEmpty()) {
            canvas.drawText("Doctor: $doctorName", 40f, y, bodyPaint)
            y += 20f
        }
        if (caregiverName.isNotEmpty()) {
            canvas.drawText("Caregiver: $caregiverName", 40f, y, bodyPaint)
            y += 20f
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 25f

        // Medicines section
        canvas.drawText("💊 Current Medicines", 40f, y, headingPaint)
        y += 20f

        if (medicines.isEmpty()) {
            canvas.drawText("No medicines added.", 40f, y, bodyPaint)
            y += 20f
        } else {
            medicines.forEach { medicine ->
                val timeStr = String.format(
                    "%02d:%02d %s",
                    if (medicine.timeHour % 12 == 0) 12 else medicine.timeHour % 12,
                    medicine.timeMinute,
                    if (medicine.timeHour < 12) "AM" else "PM"
                )
                canvas.drawText(
                    "• ${medicine.name} — $timeStr — ${medicine.remainingDays} days left — ${medicine.remainingPills} pills",
                    40f, y, bodyPaint
                )
                y += 20f
            }
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 25f

        // Appointments section
        canvas.drawText("📅 Upcoming Appointments", 40f, y, headingPaint)
        y += 20f

        if (appointments.isEmpty()) {
            canvas.drawText("No appointments scheduled.", 40f, y, bodyPaint)
            y += 20f
        } else {
            appointments.forEach { appointment ->
                val apptDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(Date(appointment.dateTimeMillis))
                canvas.drawText(
                    "• Dr. ${appointment.doctorName} (${appointment.department}) — $apptDate",
                    40f, y, bodyPaint
                )
                y += 20f
            }
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 25f

        // Symptoms section
        canvas.drawText("🏥 Recent Symptoms", 40f, y, headingPaint)
        y += 20f

        if (symptomLogs.isEmpty()) {
            canvas.drawText("No symptoms logged.", 40f, y, bodyPaint)
            y += 20f
        } else {
            symptomLogs.forEach { log ->
                val logDate = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                    .format(Date(log.timestamp))
                canvas.drawText("• ${log.symptom} — $logDate", 40f, y, bodyPaint)
                y += 20f
            }
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 25f

        // Adherence section
        canvas.drawText("📈 Adherence Summary", 40f, y, headingPaint)
        y += 20f
        val takenCount = medicines.count { it.isTaken }
        val totalCount = medicines.size
        val adherence = if (totalCount > 0) (takenCount * 100 / totalCount) else 0
        canvas.drawText("Today's adherence: $takenCount/$totalCount medicines taken ($adherence%)", 40f, y, bodyPaint)
        y += 30f

        // Footer
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 15f
        canvas.drawText("Generated by Healyn App", 40f, y, bodyPaint)

        pdfDocument.finishPage(page)

        // Save and share
        val fileName = "Healyn_Report_${SimpleDateFormat("MMM_yyyy", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Healyn Health Report - ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Health Report"))
    }
}