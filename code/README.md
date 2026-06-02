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

## 🚀 Advanced Features Roadmap

To make StreakUp a modern, robust, and advanced habit tracker, the following features can be added:

### 1. Cloud Sync & Authentication
- **Firebase / Google Sign-In:** Allow users to log in and access their data across multiple devices.
- **Cloud Database (Firestore/Supabase):** Synchronize habits, progress, and settings to the cloud in real-time.

### 2. Modern UI/UX Improvements
- **Jetpack Compose Migration:** Transition from XML Views/Fragments to Jetpack Compose for a declarative, smoother, and modern UI.
- **Material You (Dynamic Colors):** Implement Android 12+ dynamic theming for a personalized look.
- **Micro-Animations & Lottie:** Add rewarding confetti or animated checkmarks when a habit is completed to enhance user satisfaction.
- **Dark Mode Support:** A sleek dark theme tailored for night-time usage.

### 3. Advanced Tracking & Analytics
- **Detailed Charts:** Integrate charting libraries (like Vico or MPAndroidChart) to show weekly, monthly, and yearly statistics.
- **Flexible Habits:** Track habits by frequency (e.g., 3 times a week) or measurable goals (e.g., read 10 pages, drink 2L water).
- **GitHub-style Contribution Graphs:** A robust visual heatmap of daily streaks.

### 4. AI & Self-Improvement
- **AI-Powered Insights:** Use on-device or cloud AI to analyze habit patterns (e.g., "You tend to skip reading on Thursdays") and provide personalized encouragement.
- **Mood & Reflection Journaling:** Allow users to log their mood or write a quick note alongside their daily habit completion to understand what drives their success or failure.
- **Goal Chunking (Journeys):** Break down massive goals (e.g., "Run a Marathon") into a structured sequence of daily sub-habits that evolve over time.
- **Focus Timer (Pomodoro):** Built-in timers for time-based habits like studying, reading, or meditating.
- **Mindfulness Prompts:** Daily rotating gratitude or reflection prompts to promote holistic well-being alongside task tracking.

### 5. Notifications & Reminders
- **Custom Push Notifications:** Allow users to set specific reminder times for each habit using `WorkManager` or `AlarmManager`.
- **Smart Reminders:** Intelligent nudges (e.g., "You haven't completed your habit today!") triggered in the evening.

### 6. Gamification & Social Features
- **Badges & Achievements:** Unlock badges for reaching major milestones (e.g., 7 days, 30 days, 100 days).
- **Friend Accountability:** Add friends, share progress, and motivate each other.

### 7. Widgets & Wearables
- **Glance App Widgets:** Interactive Android home screen widgets to check off habits quickly without opening the app.
- **Wear OS Companion App:** View and tick off habits directly from a smartwatch.

---

## Tech Stack
- Kotlin
- Android Views + Fragments
- Material Components
- SharedPreferences (local storage)

## How to Build & Generate APK

### Option 1: Using Android Studio (Recommended)
1. Open this folder in Android Studio.
2. Let Gradle sync finish.
3. From the top menu, go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

### Option 2: Using Terminal / Command Line
If you want to generate the APK directly from your terminal without opening Android Studio, run the following commands from the project root:

**For Windows (PowerShell/CMD):**
```powershell
.\gradlew.bat assembleDebug
```

**For macOS/Linux:**
```bash
./gradlew assembleDebug
```

**Where to find the generated APK?**
Once the build completes successfully, you will find your APK file located here:
```text
app\build\outputs\apk\debug\app-debug.apk
```

*(If you want a release-ready APK, replace `assembleDebug` with `assembleRelease` in the commands above).*

## Release Build

```powershell
.\gradlew.bat assembleRelease
```

## Notes
- Min SDK: 24
- Target SDK: 36
- Main launcher activity: `SplashActivity`
