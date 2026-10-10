# CineLog

CineLog is a modern Android movie review app built with Kotlin and Jetpack Compose.  
It uses TMDB for movie discovery and Firebase for authentication, user profiles, and social reviews.

## Features

- Email/Password authentication (Firebase Auth)
- Google Sign-In (Credential Manager + Firebase Auth)
- User profiles with avatar, bio, and username
- Trending movies from TMDB
- Search movies by title and browse by category
- Movie details screen with reviews
- Create, edit, and delete reviews (backed by Firestore)
- Social reviews feed with likes and comments
- My Reviews screen
- Custom movie lists (create, manage, add/remove movies)
- Light / Dark / System theme switch (saved with DataStore)
- Settings screen with reset password, delete account, and credits
- Shimmer loading effects
- Profile setup with image cropping
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
- **Pagination:** Paging 3

## Project Setup

### 1) Clone and open

```bash
git clone https://github.com/Shahriar811/CineLog.git
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

## Credits

- **Developer:** Md. Shahriar Hossain
- **Email:** mdshahriarhossain08@gmail.com
- **GitHub:** [Shahriar811](https://github.com/Shahriar811)

## TMDB Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB.  
TMDB: [https://www.themoviedb.org/](https://www.themoviedb.org/)
