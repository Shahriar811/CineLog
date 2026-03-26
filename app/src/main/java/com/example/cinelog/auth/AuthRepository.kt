package com.example.cinelog.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signUpWithEmail(email: String, password: String)
    suspend fun sendPasswordReset(email: String)
    suspend fun signInWithGoogle(activity: Activity)
    suspend fun signOut()
}

