# Healyn — Smart Medication Tracker & Reminder

Healyn is an Android application designed to help users manage medication schedules, receive timely reminders, and keep track of their medication routines.

The project focuses on practical mobile app development, user-focused product design, local data management, and reliable background scheduling.

---

## Problem

Managing multiple medications can become difficult when prescriptions involve different timings, recurring schedules, and regular doses. Missing a dose can be easy when medication routines are not organized.

Healyn aims to simplify this process by bringing medication schedules, reminders, and basic adherence tracking into one lightweight mobile application.

---

## Key Features

- **Medication Scheduling** — Add medications with specific dosage and reminder timings.
- **Medication Reminders** — Receive scheduled notifications and alerts for upcoming doses.
- **Background Scheduling** — Uses Android scheduling mechanisms to maintain reminders even when the application is not actively open.
- **Local Data Storage** — Stores medication information and related records locally on the device.
- **Clean User Interface** — Designed for simple and distraction-free medication logging and tracking.
- **Adherence Tracking** — Provides a foundation for monitoring medication-taking patterns over time.

---

## Development Process

The project was developed using an AI-assisted development workflow combined with hands-on implementation, testing, debugging, and refinement.

### Product & System Design

- Identified the core problem and defined the primary user flows.
- Planned medication scheduling, reminder handling, and local data storage.
- Designed the database structure and overall application architecture.

### Implementation

- Used AI tools to assist with code generation, debugging, and understanding unfamiliar Android concepts.
- Integrated and adapted generated code within the Android project.
- Implemented core functionality using Jetpack Compose, Room, AlarmManager, and WorkManager.

### Testing & Debugging

- Tested medication reminders and background scheduling on physical Android hardware.
- Debugged notification and scheduling issues.
- Handled different application states, including background and closed-app scenarios.
- Iteratively refined the application based on testing results.

---

## Architecture & Tech Stack

  **Language** - Kotlin
  **UI Framework** - Jetpack Compose
  **Local Database** - Room / SQLite
  **Background Tasks** - WorkManager
  **Scheduled Reminders** - AlarmManager
  **Architecture** - MVVM
  **Development Environment** - Android Studio

---

## What I Learned

Building Healyn provided practical experience with:

- Android application architecture and lifecycle
- Kotlin and Jetpack Compose
- Local database design using Room
- Background task scheduling and notifications
- Debugging and testing on physical Android devices
- Translating a real-world problem into a functional software product
- Using AI tools as a development assistant while reviewing and validating the resulting code

---

## Screenshots

<img width="389" height="864" alt="home" src="https://github.com/user-attachments/assets/5efcc3c2-b752-4ad1-a44b-4fa9848223d9" />
<img width="238" height="531" alt="add screen" src="https://github.com/user-attachments/assets/e9fcf6c5-4685-4fea-971e-d1246a3834e7" />
<img width="389" height="864" alt="reminders" src="https://github.com/user-attachments/assets/b0358af5-5a9d-4fe4-a0b5-cc38234a6d3c" />
<img width="389" height="864" alt="alarm" src="https://github.com/user-attachments/assets/b8afe284-822a-4ba0-944e-d1f662e1aab3" />
<img width="552" height="1219" alt="alert" src="https://github.com/user-attachments/assets/4145f8c9-6edd-4fb0-84a1-7116fbb2ceee" />

---

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- Android SDK 26 or higher
- Android device or emulator

### Installation

Clone the repository:

```bash
git clone https://github.com/thenumber16/Healyn.git
