# StreakUp Habit Tracker

StreakUp is a Kotlin-based Android habit tracking app.

## Features
- Splash screen with auto-navigation
- Home screen for user name setup
- Dashboard with bottom navigation:
  - Habits list (edit/delete)
  - Add Habit
  - Tracker (monthly heatmap style)
  - Profile (edit name)
- Complete / undo complete for today
- Streak counting per habit
- Local persistence for user name, habits, streaks, and daily completion counts

## Tech Stack
- Kotlin
- Android Views + Fragments
- Material Components
- SharedPreferences (local storage)

## How to Run

### Option 1: Android Studio (recommended)
1. Open this folder in Android Studio.
2. Let Gradle sync finish.
3. Run the `app` configuration on an emulator or device.

### Option 2: Command line
From project root:

```powershell
.\gradlew.bat assembleDebug
```

Generated APK:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Release Build

```powershell
.\gradlew.bat assembleRelease
```

## Notes
- Min SDK: 24
- Target SDK: 36
- Main launcher activity: `SplashActivity`
