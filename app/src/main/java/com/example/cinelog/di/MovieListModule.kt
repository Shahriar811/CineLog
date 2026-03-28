package com.example.cinelog.di

import com.example.cinelog.lists.MovieListRepository
import com.example.cinelog.lists.MovieListRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MovieListModule {
    @Binds
    abstract fun bindMovieListRepository(impl: MovieListRepositoryImpl): MovieListRepository
}
