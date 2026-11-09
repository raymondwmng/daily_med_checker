# Daily Pill Tracker (`DailyMedChecker`)

A Native Android app built with **Kotlin** and **Jetpack Compose (Material 3)** to track daily capsule intake with an interactive blister pack view, persistent local storage, and full pack history.

## Features

- **Interactive Blister Pack View**:
  - Displays a large, realistic metallic foil blister pack with 3D two-tone capsules (default **12 capsules**, configurable from 1 to 60 in Settings).
  - **Tap any available capsule** to mark that specific slot as taken — the capsule disappears, leaving an empty popped foil cavity with the taken timestamp.
  - **Accidental Tap Undo**: Tapping an empty (already taken) slot opens a confirmation dialog to restore the capsule back into the pack and remove its timestamp record.
- **Persistent State**:
  - Automatically saves every action synchronously to local device storage (`SharedPreferences` JSON store via [`MedRepository`](app/src/main/java/com/raymondwmng/dailymedchecker/data/MedRepository.kt)) so remaining pills and history are preserved across app restarts.
- **Today's Status & Remaining Counter**:
  - Shows at a glance whether a pill has already been taken today (with time and capsule number) and how many pills remain in the active pack.
- **Scrollable History & Past Pack Archiving**:
  - Shows a scrollable log of taken dates and times for the **current pack** right below the blister pack.
  - When all capsules in the current pack are taken, displays a **"Pack Complete! Start New Pack"** prompt so you can still view the finished pack before archiving it into the expandable **Completed Past Packs** section and opening a fresh pack.
- **Configurable Pack Size**:
  - Tap the Settings icon in the top bar to change the number of capsules per pack (default 12).
  - Applies immediately if no pills have been taken in the current pack yet, or asks whether to apply to the next pack or archive & reset the current pack immediately.

## Project Structure

- [`MainActivity.kt`](app/src/main/java/com/raymondwmng/dailymedchecker/MainActivity.kt) — App entry point hosting the Compose UI.
- [`MedModels.kt`](app/src/main/java/com/raymondwmng/dailymedchecker/model/MedModels.kt) — Domain models (`CapsuleSlot`, `DoseRecord`, `CompletedPackRecord`, `MedTrackerState`) and state transitions.
- [`MedRepository.kt`](app/src/main/java/com/raymondwmng/dailymedchecker/data/MedRepository.kt) — Local persistence layer using `SharedPreferences` and `org.json`.
- [`MedViewModel.kt`](app/src/main/java/com/raymondwmng/dailymedchecker/ui/MedViewModel.kt) — State management for capsule taps, undo confirmation, pack completion, and pack size settings.
- [`BlisterPackView.kt`](app/src/main/java/com/raymondwmng/dailymedchecker/ui/components/BlisterPackView.kt) — Custom Compose Canvas & Material 3 blister pack and capsule cavity UI.
- [`Dialogs.kt`](app/src/main/java/com/raymondwmng/dailymedchecker/ui/components/Dialogs.kt) — Restore confirmation, pack size settings, and active-pack resize conflict dialogs.
- [`MainScreen.kt`](app/src/main/java/com/raymondwmng/dailymedchecker/ui/MainScreen.kt) — Main scrollable screen combining status summary, blister pack, current pack log, and expandable past packs.

## How to Open & Run

1. Open `/home/speechray/github/daily_med_checker` in **Android Studio** (Ladybug or newer recommended).
2. Let Android Studio sync Gradle and download the configured Android SDK (`compileSdk 35`).
3. Click **Run** (`Shift + F10`) on an Android Emulator or connected physical Android device (Android 8.0 / API 26+).

