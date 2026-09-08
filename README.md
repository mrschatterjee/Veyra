# Veyra

Veyra is a personal life-tracking Android app built around the idea: **Build your universe.**

## Current build

The repository contains a functional native Android implementation with:

- Cosmic / glass-inspired royal-purple interface
- Veyra opening identity sequence
- Today dashboard
- Habit creation, completion, rename and deletion
- XP, levels, progression titles and streak tracking
- Achievement unlocking
- Goal editing
- Mood tracking
- Daily journal entries
- 7D / 30D / 90D / 1Y completion statistics
- Local persistence with Android SharedPreferences
- Daily reminders with notification permission handling
- Reminder restoration after device reboot
- JSON backup and restore
- Reset and local data-management controls
- Automated unit tests for progression and core calculations
- Portrait Android experience, min SDK 26

## Open in Android Studio

1. Clone this repository.
2. Open the repository as an Android project.
3. Let Android Studio sync Gradle.
4. Run the `app` configuration on an Android device or emulator.

Package: `com.veyra.app`

## Development status

The app is in active production hardening. The remaining work is focused on deeper habit history/analytics, accessibility and visual polish, release configuration, and final device testing. Release signing is intentionally not committed to the repository.
