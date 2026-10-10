package com.example.cinelog.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.cinelog.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth?,
) : AuthRepository {

    override val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val auth = firebaseAuth
        if (auth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String) {
        requireFirebaseAuth().signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signUpWithEmail(email: String, password: String) {
        requireFirebaseAuth().createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun sendPasswordReset(email: String) {
        requireFirebaseAuth().sendPasswordResetEmail(email).await()
    }

    override suspend fun signInWithGoogle(activity: Activity) {
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        require(webClientId.isNotBlank()) {
            "Google Web client ID is missing. In Firebase Console: enable Google sign-in, add your app SHA-1, " +
                "then re-download app/google-services.json (oauth_client must not be empty), or set " +
                "GOOGLE_WEB_CLIENT_ID in local.properties to the Web client ID from Google Cloud Console."
        }

        val credentialManager = CredentialManager.create(activity)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(activity, request)
            val googleCred = GoogleIdTokenCredential.createFrom(result.credential.data)
            val firebaseCred = GoogleAuthProvider.getCredential(googleCred.idToken, null)
            requireFirebaseAuth().signInWithCredential(firebaseCred).await()
        } catch (e: GetCredentialCancellationException) {
            // User cancelled Google Account selection prompt
            return
        } catch (e: Exception) {
            val message = e.message.orEmpty()
            if (message.contains("28444", ignoreCase = true) ||
                message.contains("Developer console is not set up correctly", ignoreCase = true)
            ) {
                // If Play Services is still propagating the new SHA credentials or consent screen,
                // try fallback to GetSignInWithGoogleOption or guide user to direct Gmail
                try {
                    val fallbackOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                    val fallbackRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(fallbackOption)
                        .build()
                    val fallbackResult = credentialManager.getCredential(activity, fallbackRequest)
                    val googleCred = GoogleIdTokenCredential.createFrom(fallbackResult.credential.data)
                    val firebaseCred = GoogleAuthProvider.getCredential(googleCred.idToken, null)
                    requireFirebaseAuth().signInWithCredential(firebaseCred).await()
                    return
                } catch (fallbackCancel: GetCredentialCancellationException) {
                    return
                } catch (fallbackEx: Exception) {
                    throw IllegalStateException(
                        "Google Play Services is updating OAuth credentials. " +
                            "Please tap 'Sign in or Open account with Gmail' below to continue directly with your Gmail address.",
                        e,
                    )
                }
            }
            if (e is NoCredentialException || message.contains("No credentials available", ignoreCase = true)) {
                try {
                    val fallbackOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                    val fallbackRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(fallbackOption)
                        .build()
                    val fallbackResult = credentialManager.getCredential(activity, fallbackRequest)
                    val googleCred = GoogleIdTokenCredential.createFrom(fallbackResult.credential.data)
                    val firebaseCred = GoogleAuthProvider.getCredential(googleCred.idToken, null)
                    requireFirebaseAuth().signInWithCredential(firebaseCred).await()
                    return
                } catch (fallbackCancel: GetCredentialCancellationException) {
                    return
                } catch (fallbackEx: Exception) {
                    throw IllegalStateException(
                        "No Google account found on this device. Please use 'Sign in or Open account with Gmail' below.",
                        e,
                    )
                }
            }
            throw e
        }
    }

    override suspend fun signOut() {
        firebaseAuth?.signOut()
    }

    private fun requireFirebaseAuth(): FirebaseAuth {
        return firebaseAuth
            ?: error("Firebase is not initialized. Add app/google-services.json and rebuild the app.")
    }
}
