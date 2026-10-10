package com.example.cinelog.ui.navigation

sealed class Route(val value: String) {
    data object Auth : Route("auth")
    data object Main : Route("main")
    data object Trending : Route("trending")
    data object Search : Route("search")
    data object Lists : Route("lists")
    data object MyReviews : Route("my_reviews")
    data object Settings : Route("settings")
    data object MovieDetails : Route("movie_details/{movieId}") {
        fun create(movieId: Long) = "movie_details/$movieId"
        const val argMovieId = "movieId"
    }
    data object ListDetail : Route("list_detail/{listId}") {
        fun create(listId: String) = "list_detail/$listId"
        const val argListId = "listId"
    }
    data object UserProfile : Route("user_profile/{userId}") {
        fun create(userId: String) = "user_profile/$userId"
        const val argUserId = "userId"
    }
}
