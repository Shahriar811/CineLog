package com.example.cinelog.ui.navigation

sealed class Route(val value: String) {
    data object Auth : Route("auth")
    data object Main : Route("main")
    data object Trending : Route("trending")
    data object Search : Route("search")
    data object MyReviews : Route("my_reviews")
    data object Settings : Route("settings")
    data object MovieDetails : Route("movie_details/{movieId}") {
        fun create(movieId: Long) = "movie_details/$movieId"
        const val argMovieId = "movieId"
    }
}

