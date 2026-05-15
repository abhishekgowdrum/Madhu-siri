# Madhu-Siri: Bee-Farmer Harmony Application

## Overview
**Madhu-Siri** is a community-powered Android application designed to protect bees by bridging the gap between beekeepers and farmers. It solves the problem of accidental bee mortality caused by crop spraying by providing a real-time coordination platform. Farmers can report when and where they intend to spray, and beekeepers can pin their hive locations to receive proximity-based alerts.

## Features
- **Role-Based Profiles:** Choice between **Beekeeper** and **Farmer** roles with specialized dashboards.
- **Interactive Map:** Google Maps integration for pinning hives and marking spraying zones.
- **Proximity Alerts:** Real-time Firebase notifications when spraying is reported within range of a hive.
- **Spray Reports:** Farmers can quickly mark a location on the map to notify nearby beekeepers.
- **Bee-Friendly Tips:** Educational resources on bee health and safe pesticide usage.
- **Location Detection:** Automatically detects your current village to streamline setup.

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM
- **Backend:** Firebase (Authentication, Realtime Database, Cloud Messaging)
- **Maps:** Google Maps SDK for Android

## Setup Instructions

### 1. Firebase Configuration
To use this project, you need to configure Firebase:
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new project (e.g., "Madhu-Siri").
3. Add an Android app with the package name `com.example.madhusiri`.
4. Download the `google-services.json` file and place it in the `app/` directory.
5. Enable **Email/Password** authentication in the Firebase Console.
6. Enable **Realtime Database** and set rules:
   ```json
   {
     "rules": {
       ".read": "auth != null",
       ".write": "auth != null"
     }
   }
   ```

### 2. Google Maps Configuration
1. Obtain an API Key from the [Google Cloud Console](https://console.cloud.google.com/).
2. Enable the **Maps SDK for Android**.
3. Open `app/src/main/AndroidManifest.xml` and replace `YOUR_GOOGLE_MAPS_API_KEY` with your actual key.

### 3. Build and Run
1. Open the project in **Android Studio**.
2. Sync the project with Gradle files.
3. Build and run on an emulator or physical device (Android 8.0+ required).

---
*Created for the Bee-Farmer Harmony Initiative.*
