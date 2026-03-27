# CineLog

CineLog is a modern Android movie review app built with Kotlin and Jetpack Compose.  
It uses TMDB for movie discovery and Firebase for authentication and user reviews.

## Features

- Email/Password authentication (Firebase Auth)
- Google Sign-In (Credential Manager + Firebase Auth)
- Trending movies from TMDB
- Search movies by title
- Movie details screen
- Light / Dark / System theme switch (saved with DataStore)
- Settings screen with credits section
- Custom launcher icon

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** ViewModel + Repository + DI
- **Dependency Injection:** Hilt
- **Networking:** Retrofit + OkHttp + Kotlinx Serialization
- **Image Loading:** Coil
- **Backend:** Firebase Auth + Firestore
- **Navigation:** Navigation Compose
- **Storage:** DataStore Preferences

## Project Setup

### 1) Clone and open

```bash
git clone <your-repo-url>
cd CineLog
```

Open in Android Studio (latest stable recommended).

### 2) Add local secrets

In `local.properties`, add:

```properties
TMDB_API_KEY=your_tmdb_api_key
GOOGLE_WEB_CLIENT_ID=your_web_client_id.apps.googleusercontent.com
```

> Do not commit `local.properties` with real keys.

### 3) Configure Firebase

1. Create/select your Firebase project.
2. Add an Android app with package:
   - `com.example.cinelog`
3. Download `google-services.json`.
4. Place it at:
   - `app/google-services.json`
5. In Firebase Authentication, enable:
   - Email/Password
   - Google

### 4) Build and run

```bash
./gradlew :app:assembleDebug
```

Run the app on an emulator or Android device.

## Current Status

Implemented:
- Auth flow screens and wiring
- Trending/Search/Details screens with TMDB integration
- Settings screen (theme options + credits)

In progress / next:
- Firestore review create/edit/delete flow
- My Reviews screen backed by Firestore
- Full account actions in Settings (change password, delete account, etc.)

## Credits

- **Developer:** Md. Shahriar Hossain
- **Email:** mdshahriarhossain08@gmail.com
- **GitHub:** [Shahriar811](https://github.com/Shahriar811)

## TMDB Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB.  
TMDB: [https://www.themoviedb.org/](https://www.themoviedb.org/)

