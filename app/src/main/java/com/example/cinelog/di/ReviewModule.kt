package com.example.cinelog.di

import android.content.Context
import com.example.cinelog.reviews.ReviewRepository
import com.example.cinelog.reviews.ReviewRepositoryImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {
    @Provides
    @Singleton
    fun provideFirestore(
        @ApplicationContext context: Context,
    ): FirebaseFirestore? {
        val app = FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(context)
        return app?.let { FirebaseFirestore.getInstance(it) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ReviewModule {
    @Binds
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository
}

