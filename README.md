# StreakUp Habit Tracker

StreakUp is a modern, Kotlin-based Android habit tracking application designed to help users build better habits one day at a time. Track your daily habits, keep quick notes, and monitor your consistency with visual streaks and progress tracking.

## 🌟 Key Features
- **Daily Habit Tracking:** Easily create, edit, and complete daily habits.
- **Notes & Journaling:** Keep track of your daily thoughts alongside your habit progress.
- **Streak Counters:** Visual flame indicators keep you motivated to not break the chain.
- **Monthly Progress Heatmap:** See your consistency over time with a visual tracker.
- **Smart Reminders:** Get notified before you lose a streak.
- **AI Coach Insights:** Use the integrated AI coach features (via Ollama/Ngrok) to get personalized insights, break down big goals, or draft habits via voice.

## 🛠 Tech Stack
- **Language:** Kotlin
- **Architecture:** MVVM, View Binding
- **UI:** Android XML Layouts + Material Design Components
- **Storage:** SharedPreferences for local persistence
- **Authentication:** Firebase & Google Sign-In
- **AI Integration:** Retrofit (connecting to a local/remote Ollama instance via Ngrok)

## 🚀 How to Run the Project

### Option 1: Using Android Studio (Recommended)
1. Open **Android Studio**.
2. Select **Open an existing project** and navigate to the `code/` folder inside this repository (`d:\Github\streak-up-habbit-tracker\code`).
3. Allow Gradle to sync and download all dependencies.
4. Connect an Android device via USB or start an Android Emulator.
5. Click the **Run** ▶️ button in the toolbar, or go to **Build > Build Bundle(s) / APK(s) > Build APK(s)** to generate an installable APK.

### Option 2: Using the Command Line
If you want to generate the APK directly from your terminal without opening Android Studio, run the following commands from the project root:

1. Navigate into the source code directory:
```bash
cd code
```

2. Build the Debug APK:

**For Windows (PowerShell/CMD):**
```powershell
.\gradlew.bat assembleDebug
```

**For macOS/Linux:**
```bash
./gradlew assembleDebug
```

3. **Locate the APK:**
Once the build completes successfully, you will find your APK file located at:
```text
code\app\build\outputs\apk\debug\app-debug.apk
```
You can transfer this `.apk` file to your Android device to install and run the app.

*(If you want a release-ready APK, replace `assembleDebug` with `assembleRelease` in the commands above).*

## 📌 Requirements
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36
- **Gradle:** 8.x

## 🔧 AI Coach Setup (Optional)
To use the AI Coach features, you need to provide an Ngrok URL to a running Ollama server:
1. Run an Ollama server locally with a model like `llama3`.
2. Expose the local server to the internet using Ngrok (`ngrok http 11434`).
3. Open the **Profile** tab in the app.
4. Paste your Ngrok URL into the "AI & Sync Settings" section and save it.
