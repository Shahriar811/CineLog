package com.example.cinelog.di

import android.content.Context
import com.example.cinelog.auth.AuthRepository
import com.example.cinelog.auth.AuthRepositoryImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(
        @ApplicationContext context: Context,
    ): FirebaseAuth? {
        val app = FirebaseApp.initializeApp(context) ?: FirebaseApp.getApps(context).firstOrNull()
        return app?.let { FirebaseAuth.getInstance(it) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

