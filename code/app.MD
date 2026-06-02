# StreakUp App Component Guide

This document explains the full app composition and flow, from launch (splash) to daily usage and background reminders.

## 1) End-to-End Runtime Flow

1. App launch starts at `SplashActivity` (declared as LAUNCHER in manifest).
2. `SplashActivity` initializes `HabitRepository`, creates reminder notification channel, schedules reminder worker, waits ~1.4s, then routes:
   - to `DashboardActivity` if a user name already exists
   - to `HomeActivity` if user name is missing
3. In `HomeActivity`, user enters name and taps Start.
4. `DashboardActivity` loads with bottom navigation and default tab `HabitsFragment`.
5. User actions:
   - Add habits in `AddHabitFragment`
   - View/edit/delete/complete habits in `HabitsFragment` via `HabitAdapter`
   - See monthly completion heatmap in `TrackerFragment` via `TrackerHeatmapAdapter`
   - Edit profile name in `ProfileFragment`
6. All habit/name/completion data is saved in SharedPreferences by `HabitRepository`.
7. Background reminders:
   - `StreakReminderScheduler` enqueues periodic WorkManager job.
   - `StreakReminderWorker` checks habits at risk (completed yesterday, not today).
   - `StreakReminderNotifier` posts a notification (when permission is granted).

---

## 2) Core App and Navigation Components

### `app/src/main/AndroidManifest.xml`
- What it does:
  - Declares app theme/icons/backup rules.
  - Registers activities (`SplashActivity`, `HomeActivity`, `DashboardActivity`).
  - Defines launcher entry (`SplashActivity`).
  - Declares notification permission (`POST_NOTIFICATIONS`).
- How used:
  - Android system reads this first at install/run time.
- Why used:
  - Mandatory app registration, entry point, permissions, and app-level metadata.

### `app/src/main/java/com/example/streakup_habbit_tracker/SplashActivity.kt`
- What it does:
  - Initializes repository.
  - Ensures reminder notification channel exists.
  - Schedules periodic reminder work.
  - Delays and routes to Home/Dashboard.
- How used:
  - First screen after process start.
- Why used:
  - Central bootstrapping point for data readiness + app routing + reminder setup.

### `app/src/main/java/com/example/streakup_habbit_tracker/HomeActivity.kt`
- What it does:
  - Collects and validates user name.
  - Saves name to repository and opens dashboard.
- How used:
  - Shown only when user name is empty.
- Why used:
  - Onboarding gate and profile personalization setup.

### `app/src/main/java/com/example/streakup_habbit_tracker/DashboardActivity.kt`
- What it does:
  - Hosts toolbar + fragment container + bottom navigation.
  - Handles tab switching among Habits/Add/Tracker/Profile fragments.
  - Requests runtime notification permission on Android 13+.
- How used:
  - Primary shell screen after onboarding.
- Why used:
  - Keeps navigation/state orchestration centralized and simple.

### `app/src/main/res/menu/dashboard_bottom_nav.xml`
- What it does:
  - Defines the 4 dashboard tabs and icons.
- How used:
  - Bound to `BottomNavigationView` in dashboard layout.
- Why used:
  - Declarative menu resource keeps tab config separate from code.

---

## 3) Habit Domain and Persistence Components

### `app/src/main/java/com/example/streakup_habbit_tracker/data/Habit.kt`
- What it does:
  - Defines habit data model (`id`, `title`, `note`, `streakCount`, date fields for undo logic).
- How used:
  - Used across repository, adapter, and fragments.
- Why used:
  - Single source model for all habit operations and rendering.

### `app/src/main/java/com/example/streakup_habbit_tracker/data/HabitRepository.kt`
- What it does:
  - Stores/loads user name, habits, and daily completion counts from SharedPreferences.
  - Implements all habit operations: add/update/delete/toggle/complete-bulk.
  - Manages streak progression and undo-completion behavior.
  - Exposes daily count queries used by tracker heatmap.
- How used:
  - Called by all UI layers (activities/fragments/adapters/worker).
- Why used:
  - Centralized app data logic, persistence, and consistency guarantees.

---

## 4) Feature Fragments and UI Controllers

### `app/src/main/java/com/example/streakup_habbit_tracker/ui/AddHabitFragment.kt`
- What it does:
  - Captures title/note, validates input, creates habit.
  - Shows snackbar and switches to Habits tab after successful add.
- How used:
  - Loaded from dashboard bottom navigation.
- Why used:
  - Dedicated creation flow keeps add logic isolated.

### `app/src/main/java/com/example/streakup_habbit_tracker/ui/HabitsFragment.kt`
- What it does:
  - Displays habits list with empty state.
  - Supports edit/delete/toggle complete and bulk complete.
  - Shows dialogs/toasts and updates summary header.
- How used:
  - Default tab in dashboard.
- Why used:
  - Main daily interaction surface for habit management.

### `app/src/main/java/com/example/streakup_habbit_tracker/ui/HabitAdapter.kt`
- What it does:
  - RecyclerView adapter for habit cards.
  - Renders visual states (selected/completed/ready).
  - Handles row-level actions and checkbox selection.
- How used:
  - Owned by `HabitsFragment`.
- Why used:
  - Keeps row rendering and selection state logic out of fragment.

### `app/src/main/java/com/example/streakup_habbit_tracker/ui/TrackerFragment.kt`
- What it does:
  - Builds month calendar cells with leading/trailing placeholders.
  - Reads completion counts per date.
  - Handles month navigation and binds heatmap adapter.
- How used:
  - Dashboard tracker tab.
- Why used:
  - Encapsulates calendar-month computation and visualization orchestration.

### `app/src/main/java/com/example/streakup_habbit_tracker/ui/TrackerHeatmapAdapter.kt`
- What it does:
  - Binds `TrackerDayCell` items to day cards.
  - Calculates color intensity level from completion ratio.
  - Adds accessibility descriptions and highlights today.
- How used:
  - Used by `TrackerFragment` RecyclerView grid.
- Why used:
  - Isolates heatmap cell rendering from calendar generation logic.

### `app/src/main/java/com/example/streakup_habbit_tracker/ui/TrackerDayCell.kt`
- What it does:
  - Data holder for tracker grid cell (label/date/count/isToday).
- How used:
  - Passed from fragment to heatmap adapter.
- Why used:
  - Clear contract between date generation and UI rendering.

### `app/src/main/java/com/example/streakup_habbit_tracker/ui/ProfileFragment.kt`
- What it does:
  - Displays greeting and habit count summary.
  - Opens edit-name dialog and saves updates.
- How used:
  - Dashboard profile tab.
- Why used:
  - Keeps profile editing and summary independent from other tabs.

---

## 5) Reminder and Notification Components

### `app/src/main/java/com/example/streakup_habbit_tracker/reminders/StreakReminderScheduler.kt`
- What it does:
  - Schedules unique periodic WorkManager job (`streak_reminder_daily`).
  - Computes initial delay to run around 20:00 local time.
- How used:
  - Called during app startup from `SplashActivity`.
- Why used:
  - Reliable background scheduling across process death/restart.

### `app/src/main/java/com/example/streakup_habbit_tracker/reminders/StreakReminderWorker.kt`
- What it does:
  - Loads habits and selects at-risk streaks (`streakCount > 0`, completed yesterday).
  - Triggers reminder notification when needed.
- How used:
  - Executed by WorkManager on schedule.
- Why used:
  - Background-safe place to run periodic reminder business logic.

### `app/src/main/java/com/example/streakup_habbit_tracker/reminders/StreakReminderNotifier.kt`
- What it does:
  - Creates notification channel for Android O+.
  - Builds and posts reminder notification (single/multiple habit text).
  - Opens `DashboardActivity` when notification is tapped.
- How used:
  - Called from worker and startup bootstrap.
- Why used:
  - Centralized notification policy and message formatting.

---

## 6) Layout Components (Screen and Dialog XML)

### Activity layouts

#### `app/src/main/res/layout/activity_splash.xml`
- Working:
  - Gradient background, app icon card, title/subtitle, progress indicator.
- Used by:
  - `SplashActivity`.
- Why:
  - Branded startup feedback while bootstrapping data and routing.

#### `app/src/main/res/layout/activity_home.xml`
- Working:
  - Name input card + Start button.
- Used by:
  - `HomeActivity`.
- Why:
  - Lightweight onboarding entry.

#### `app/src/main/res/layout/activity_dashboard.xml`
- Working:
  - Toolbar + fragment container + bottom navigation shell.
- Used by:
  - `DashboardActivity`.
- Why:
  - Shared host layout for all main app tabs.

### Fragment layouts

#### `app/src/main/res/layout/fragment_habits.xml`
- Working:
  - Header card, summary, bulk-complete button, recycler list, empty state.
- Used by:
  - `HabitsFragment`.
- Why:
  - Combines status summary and list interaction in one surface.

#### `app/src/main/res/layout/fragment_add_habit.xml`
- Working:
  - Title input, note input, add button.
- Used by:
  - `AddHabitFragment`.
- Why:
  - Focused create form.

#### `app/src/main/res/layout/fragment_tracker.xml`
- Working:
  - Month navigation, weekday headers, heatmap recycler, legend.
- Used by:
  - `TrackerFragment`.
- Why:
  - Gives compact monthly consistency visualization.

#### `app/src/main/res/layout/fragment_profile.xml`
- Working:
  - Greeting card, edit-name action, progress snapshot card.
- Used by:
  - `ProfileFragment`.
- Why:
  - Personal stats and identity edits in one place.

### Recycler item layouts

#### `app/src/main/res/layout/item_habit.xml`
- Working:
  - Habit card row with checkbox, edit/delete icons, note, status chip, streak text, complete button.
- Used by:
  - `HabitAdapter`.
- Why:
  - Rich row interaction for daily habit operations.

#### `app/src/main/res/layout/item_tracker_day.xml`
- Working:
  - Single day card cell for heatmap grid.
- Used by:
  - `TrackerHeatmapAdapter`.
- Why:
  - Reusable atomic unit for monthly heatmap rendering.

### Dialog layouts

#### `app/src/main/res/layout/dialog_edit_habit.xml`
- Working:
  - Input fields for editing title and note.
- Used by:
  - `HabitsFragment` edit dialog.
- Why:
  - Separate, reusable edit form view.

#### `app/src/main/res/layout/dialog_edit_name.xml`
- Working:
  - Name input for profile edit dialog.
- Used by:
  - `ProfileFragment`.
- Why:
  - Keeps profile edit UI simple and modular.

---

## 7) Styling, Visual Assets, and Theming Components

### `app/src/main/res/values/colors.xml`
- Working:
  - Central color tokens for brand, surfaces, text, tracker levels, state colors.
- Used by:
  - All layouts/adapters/themes/drawables.
- Why:
  - Consistent design language and easier future theming.

### `app/src/main/res/values/themes.xml`
### `app/src/main/res/values-night/themes.xml`
- Working:
  - App-wide MaterialComponents theme values.
- Used by:
  - Manifest app theme declaration.
- Why:
  - Uniform style baseline across all screens.

### `app/src/main/res/values/strings.xml`
- Working:
  - All UI strings and plurals (including reminder/permission text).
- Used by:
  - Activities/fragments/adapters/notifications.
- Why:
  - Centralized copy, plural handling, localization readiness.

### `app/src/main/res/color/bottom_nav_item_tint.xml`
- Working:
  - Selector for selected/unselected bottom nav tint.
- Used by:
  - `BottomNavigationView` in dashboard layout.
- Why:
  - Declarative state-based nav styling.

### `app/src/main/res/drawable/bg_splash_gradient.xml`
- Working:
  - Splash linear gradient background.
- Used by:
  - `activity_splash.xml`.
- Why:
  - Brand-first startup look.

### `app/src/main/res/drawable/bg_habits_header_card.xml`
- Working:
  - Layer-list with decorative glow circles.
- Used by:
  - Habits header section.
- Why:
  - Visual hierarchy and emphasis for habits summary area.

### `app/src/main/res/drawable/bg_habit_status_chip.xml`
- Working:
  - Rounded chip background shape.
- Used by:
  - Habit status label.
- Why:
  - Distinct status pill styling.

### `app/src/main/res/drawable/bg_habit_icon_button.xml`
- Working:
  - Ripple oval background for edit/delete icons.
- Used by:
  - Habit row action buttons.
- Why:
  - Touch feedback and consistent icon button affordance.

### Launcher icon resources
- Files:
  - `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
  - `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
  - `app/src/main/res/drawable/ic_launcher_background.xml`
  - `app/src/main/res/drawable/ic_launcher_foreground.xml`
  - `app/src/main/res/mipmap-*/ic_launcher*.webp`
- Working:
  - Adaptive launcher icon definitions and density-specific assets.
- Used by:
  - Home screen/app drawer icon rendering.
- Why:
  - Proper Android launcher compatibility across devices.

---

## 8) Backup and Data Transfer Components

### `app/src/main/res/xml/backup_rules.xml`
### `app/src/main/res/xml/data_extraction_rules.xml`
- Working:
  - Placeholder configs for cloud backup and device transfer policies.
- Used by:
  - Referenced from manifest app block.
- Why:
  - Future-ready backup customization entry points.

---

## 9) Build and Dependency Components

### `app/build.gradle.kts`
- Working:
  - Android app module config (sdk levels, build types, Java compatibility).
  - App dependencies (AppCompat, Material, RecyclerView, WorkManager, tests).
- Used by:
  - Gradle build system for compile/package.
- Why:
  - Defines module build behavior and libraries.

### `gradle/libs.versions.toml`
- Working:
  - Centralized dependency/version catalog.
- Used by:
  - Gradle aliases in module/build scripts.
- Why:
  - Better dependency management and version consistency.

### `build.gradle.kts` (root)
- Working:
  - Top-level plugin declaration.
- Used by:
  - Multi-module Gradle setup.
- Why:
  - Root build orchestration.

### `settings.gradle.kts`
- Working:
  - Includes app module and repository management.
- Used by:
  - Gradle project discovery.
- Why:
  - Defines project structure.

---

## 10) Why This Structure Works

- Clear separation of concerns:
  - Activity shell/navigation
  - Feature fragments
  - Shared data repository
  - Background reminder subsystem
  - XML resource-driven UI and style
- Low complexity persistence:
  - SharedPreferences is sufficient for local single-user habit tracking.
- Good maintainability:
  - Most behavior is in small, focused files.
- User-centric flow:
  - Splash bootstrap -> onboarding (if needed) -> dashboard tabs -> daily actions + passive reminders.

---

## 11) Quick Component Checklist

If you want to extend the app, these are the primary change points:

- Add new tab/screen: `DashboardActivity.kt`, `dashboard_bottom_nav.xml`, new Fragment + layout.
- Change streak logic: `HabitRepository.kt`.
- Change reminder timing/rules: `StreakReminderScheduler.kt` and `StreakReminderWorker.kt`.
- Change notification content/channel behavior: `StreakReminderNotifier.kt` and `strings.xml`.
- Change look and feel: `colors.xml`, `themes.xml`, drawables, and layout XML files.
