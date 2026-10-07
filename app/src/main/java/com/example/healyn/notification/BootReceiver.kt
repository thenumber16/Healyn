package com.example.healyn.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.healyn.data.HealynDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = HealynDatabase.getDatabase(context).healynDao()

                // Reschedule all medicine reminders
                val medicines = dao.getAllMedicines().first()
                medicines.forEach { medicine ->
                    AlarmScheduler.scheduleMedicineAlarm(context, medicine)
                }

                // Reschedule all appointment reminders
                val appointments = dao.getAllAppointments().first()
                appointments.forEach { appointment ->
                    AlarmScheduler.scheduleAppointmentAlarm(context, appointment)
                }
            }
        }
    }
}