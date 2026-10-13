package com.example.cinelog.di

import com.example.cinelog.social.SocialRepository
import com.example.cinelog.social.SocialRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SocialModule {
    @Binds
    @Singleton
    abstract fun bindSocialRepository(impl: SocialRepositoryImpl): SocialRepository
}
