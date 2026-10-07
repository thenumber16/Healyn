# Healyn — Smart Medication Tracker & Reminder

Healyn is an Android app that helps users manage medication schedules, get reliable reminders, and track their routine, with caregiver alerts when a dose is missed.

> **Disclaimer:** Healyn is an academic project. It is not a medical device and is not a substitute for professional medical advice.

---

## Problem

Managing multiple medications is hard when prescriptions have different timings, recurring schedules, and regular doses. Missing a dose is easy when routines aren't organized.

Healyn brings schedules, reminders, refill alerts, and adherence tracking into one lightweight app, and lets a caregiver know when a dose is missed.

---

## Key Features

### Medication Management
- **Scheduling** — add medicines with dosage and reminder times (native time picker).
- **Today's Schedule** — see today's doses and mark each as taken.
- **Pills Countdown & Refill Alerts** — get an alert when remaining pills run out before the remaining days do.
- **Safe Deletion** — delete with confirmation.

### Reminders & Alarms
- **Notifications with sound** for every scheduled dose.
- **Full-screen lock-screen alarm** with snooze options (15 min, 30 min, 1 hr, or custom).
- **Missed-dose alert** after 30 minutes, with an automatic SMS to a caregiver.
- **Appointment reminders** one day before, with a native date picker.
- **Reboot-proof alarms** — a boot receiver restores scheduled alarms after a restart.
- **Midnight reset** of daily status using WorkManager.

### Tracking & Reports
- **Adherence streak card** to show consistency over time.
- **Symptom logger** — five symptoms with timestamped logs.
- **Monthly PDF health report** that can be shared.

### Convenience
- **Quick Call card** for one-tap access to your doctor or caregiver.
- **Settings screen** for patient, doctor, and caregiver details.
- **Local-first storage** — all data stays on the device.

---

## Screenshots

| Home | Add Medicine | Reminders | Alarm | Alert |
|:---:|:---:|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/5efcc3c2-b752-4ad1-a44b-4fa9848223d9" width="180" alt="Healyn home screen showing today's schedule" /> | <img src="https://github.com/user-attachments/assets/e9fcf6c5-4685-4fea-971e-d1246a3834e7" width="180" alt="Add medicine screen" /> | <img src="https://github.com/user-attachments/assets/b0358af5-5a9d-4fe4-a0b5-cc38234a6d3c" width="180" alt="Reminders screen" /> | <img src="https://github.com/user-attachments/assets/b8afe284-822a-4ba0-944e-d1f662e1aab3" width="180" alt="Full-screen medication alarm" /> | <img src="https://github.com/user-attachments/assets/4145f8c9-6edd-4fb0-84a1-7116fbb2ceee" width="180" alt="Missed dose alert" /> |

---

## Architecture & Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose, Navigation Compose |
| Local database | Room (SQLite), with 3 migrations |
| Preferences | DataStore |
| Background tasks | WorkManager |
| Exact reminders | AlarmManager |
| Architecture | MVVM |
| Build | Gradle 8.7, AGP 8.4.0, KSP |
| IDE | Android Studio |

---

## Development Process

Healyn was built with an AI-assisted workflow combined with hands-on implementation, testing, and debugging.

**Product & system design**
- Defined the core problem and primary user flows.
- Planned scheduling, reminder handling, and local data storage.
- Designed the database structure and overall architecture.

**Implementation**
- Used AI tools for code generation, debugging, and learning unfamiliar Android concepts.
- Integrated and adapted generated code within the project.
- Built core functionality with Jetpack Compose, Room, AlarmManager, and WorkManager.

**Testing & debugging**
- Tested reminders and background scheduling on physical Android hardware.
- Debugged notification and alarm issues across foreground, background, closed-app, and reboot scenarios.
- Refined the app iteratively based on test results.

---

## What I Learned

- Android app architecture and lifecycle
- Kotlin and Jetpack Compose
- Local database design and migrations with Room
- Reliable background scheduling: AlarmManager vs. WorkManager, and surviving reboots
- Notifications, full-screen intents, and permissions
- Debugging and testing on physical devices
- Turning a real-world problem into a working product
- Using AI as a development assistant while reviewing and validating the output

---

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- Android SDK 26 or higher
- An Android device or emulator

### Installation

```bash
git clone https://github.com/thenumber16/Healyn.git
cd Healyn
```

1. Open the `Healyn` folder in Android Studio.
2. Wait for Gradle to sync.
3. Run the app on a device or emulator.

### Permissions

On first run, Healyn will ask for:

- **Notifications** — for dose and appointment reminders
- **Exact alarms** — so reminders fire on time
- **SMS** — to message your caregiver when a dose is missed

For the most reliable alarms, test on a physical device and exclude the app from battery optimization.



## Author

**Kaushal**

## License

Add a `LICENSE` file (for example MIT) and reference it here.
