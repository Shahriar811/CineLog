import groovy.json.JsonSlurper
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Lets the project build before Firebase is configured.
// When you add `app/google-services.json`, the plugin will be applied automatically.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { input -> load(input) }
    }
}

/** Web client ID (client_type 3) for Google Sign-In — from Firebase google-services.json when OAuth clients are provisioned. */
@Suppress("UNCHECKED_CAST")
fun readGoogleWebClientIdFromGoogleServices(): String {
    val f = file("google-services.json")
    if (!f.exists()) return ""
    val root = JsonSlurper().parse(f) as Map<String, Any>
    val clients = root["client"] as? List<Map<String, Any>> ?: return ""
    for (client in clients) {
        val oauth = client["oauth_client"] as? List<Map<String, Any>>
        oauth?.forEach { o ->
            val type = (o["client_type"] as? Number)?.toInt() ?: return@forEach
            if (type == 3) return (o["client_id"] as? String)?.trim().orEmpty()
        }
        val services = client["services"] as? Map<String, Any>
        val appinvite = services?.get("appinvite_service") as? Map<String, Any>
        val other = appinvite?.get("other_platform_oauth_client") as? List<Map<String, Any>>
        other?.forEach { o ->
            val type = (o["client_type"] as? Number)?.toInt() ?: return@forEach
            if (type == 3) return (o["client_id"] as? String)?.trim().orEmpty()
        }
    }
    return ""
}

android {
    namespace = "com.example.cinelog"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.cinelog"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val tmdbApiKey = (localProperties["TMDB_API_KEY"] as String?)?.trim().orEmpty()
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
        buildConfigField("String", "TMDB_BASE_URL", "\"https://api.themoviedb.org/3/\"")

        val googleWebClientId = (localProperties["GOOGLE_WEB_CLIENT_ID"] as String?)?.trim().orEmpty()
            .ifBlank { readGoogleWebClientIdFromGoogleServices() }
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.org.jetbrains.kotlinx.coroutines.android)
    implementation(libs.org.jetbrains.kotlinx.coroutines.play.services)

    implementation(libs.io.coil.kt.coil.compose)

    implementation(libs.com.squareup.retrofit2.retrofit)
    implementation(libs.com.jakewharton.retrofit2.kotlinx.serialization.converter)
    implementation(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.com.squareup.okhttp3.logging.interceptor)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    implementation(libs.com.google.dagger.hilt.android)
    kapt(libs.com.google.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.com.google.android.libraries.identity.googleid)
    implementation(libs.com.google.android.gms.play.services.auth)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}