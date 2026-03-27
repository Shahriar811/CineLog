package com.example.cinelog.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.cinelog.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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
            "GOOGLE_WEB_CLIENT_ID is missing. Add it to local.properties as GOOGLE_WEB_CLIENT_ID=..."
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(activity)
        val result = credentialManager.getCredential(activity, request)

        val googleCred = GoogleIdTokenCredential.createFrom(result.credential.data)
        val firebaseCred = GoogleAuthProvider.getCredential(googleCred.idToken, null)
        requireFirebaseAuth().signInWithCredential(firebaseCred).await()
    }

    override suspend fun signOut() {
        firebaseAuth?.signOut()
    }

    private fun requireFirebaseAuth(): FirebaseAuth {
        return firebaseAuth
            ?: error("Firebase is not initialized. Add app/google-services.json and rebuild the app.")
    }
}

