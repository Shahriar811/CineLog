package com.example.cinelog.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cinelog.auth.AuthViewModel
import com.example.cinelog.ui.components.AppScaffold
import com.example.cinelog.ui.details.MovieDetailsScreen
import com.example.cinelog.ui.lists.ListDetailScreen
import com.example.cinelog.ui.lists.ListsScreen
import com.example.cinelog.ui.myreviews.MyReviewsScreen
import com.example.cinelog.ui.navigation.Route
import com.example.cinelog.ui.search.SearchScreen
import com.example.cinelog.ui.settings.SettingsScreen
import com.example.cinelog.ui.trending.TrendingScreen

private data class BottomItem(
    val route: Route,
    val label: String,
    val icon: @Composable () -> Unit,
)

fun androidx.navigation.NavGraphBuilder.mainGraph(
    navController: NavHostController,
    onSignedOut: () -> Unit,
) {
    composable(Route.Main.value) {
        val mainNavController = rememberNavController()
        val authVm: AuthViewModel = hiltViewModel()

        val items = listOf(
            BottomItem(Route.Trending, "Trending") {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null)
            },
            BottomItem(Route.Search, "Search") { Icon(Icons.Default.Search, contentDescription = null) },
            BottomItem(Route.Lists, "Lists") { Icon(Icons.Default.Bookmarks, contentDescription = null) },
            BottomItem(Route.MyReviews, "Reviews") { Icon(Icons.Default.Person, contentDescription = null) },
            BottomItem(Route.Settings, "Settings") { Icon(Icons.Default.Settings, contentDescription = null) },
        )

        val backStackEntry by mainNavController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        AppScaffold(
            title = items.firstOrNull { it.route.value == currentRoute }?.label ?: "CineLog",
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 6.dp,
                ) {
                    items.forEach { item ->
                        val selected = currentRoute == item.route.value
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                mainNavController.navigate(item.route.value) {
                                    popUpTo(mainNavController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = item.icon,
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = mainNavController,
                startDestination = Route.Trending.value,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Route.Trending.value) {
                    TrendingScreen(
                        onMovieClick = { movieId ->
                            navController.navigate(Route.MovieDetails.create(movieId))
                        },
                    )
                }
                composable(Route.Search.value) {
                    SearchScreen(
                        onMovieClick = { movieId ->
                            navController.navigate(Route.MovieDetails.create(movieId))
                        },
                    )
                }
                composable(Route.Lists.value) {
                    ListsScreen(
                        onListClick = { listId ->
                            navController.navigate(Route.ListDetail.create(listId))
                        },
                    )
                }
                composable(Route.MyReviews.value) {
                    MyReviewsScreen(
                        onMovieClick = { movieId ->
                            navController.navigate(Route.MovieDetails.create(movieId))
                        },
                    )
                }
                composable(Route.Settings.value) {
                    SettingsScreen(
                        onSignOut = {
                            authVm.signOut()
                            onSignedOut()
                        },
                    )
                }
            }
        }
    }

    composable(
        route = Route.MovieDetails.value,
        arguments = listOf(navArgument(Route.MovieDetails.argMovieId) { type = NavType.LongType }),
    ) { backStackEntry ->
        val movieId = backStackEntry.arguments?.getLong(Route.MovieDetails.argMovieId) ?: return@composable
        MovieDetailsScreen(movieId = movieId)
    }

    composable(
        route = Route.ListDetail.value,
        arguments = listOf(navArgument(Route.ListDetail.argListId) { type = NavType.StringType }),
    ) {
        ListDetailScreen(
            onMovieClick = { movieId ->
                navController.navigate(Route.MovieDetails.create(movieId))
            },
        )
    }
}
