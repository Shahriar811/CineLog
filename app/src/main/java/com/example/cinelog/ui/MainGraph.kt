package com.example.cinelog.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cinelog.auth.AuthViewModel
import com.example.cinelog.profile.ProfileSetupDialog
import com.example.cinelog.ui.components.AppScaffold
import com.example.cinelog.ui.details.MovieDetailsScreen
import com.example.cinelog.ui.lists.ListDetailScreen
import com.example.cinelog.ui.lists.ListsScreen
import com.example.cinelog.ui.myreviews.MyReviewsScreen
import com.example.cinelog.ui.navigation.Route
import com.example.cinelog.ui.profile.UserProfileScreen
import com.example.cinelog.ui.search.SearchScreen
import com.example.cinelog.ui.settings.SettingsScreen
import com.example.cinelog.ui.theme.CinemaAmber
import com.example.cinelog.ui.trending.TrendingScreen

private data class NavItemData(
    val route: Route,
    val label: String,
    val icon: ImageVector,
)

fun androidx.navigation.NavGraphBuilder.mainGraph(
    navController: NavHostController,
    onSignedOut: () -> Unit,
) {
    composable(Route.Main.value) {
        val mainNavController = rememberNavController()
        val authVm: AuthViewModel = hiltViewModel()
        val mainVm: MainViewModel = hiltViewModel()

        val userProfile by mainVm.userProfile.collectAsState()
        val isCheckingProfile by mainVm.isCheckingProfile.collectAsState()

        if (!isCheckingProfile && (userProfile == null || userProfile?.username.isNullOrBlank())) {
            ProfileSetupDialog(
                initialDisplayName = mainVm.firebaseAuth?.currentUser?.displayName.orEmpty(),
                profileRepository = mainVm.profileRepository,
                onProfileCreated = { createdProfile ->
                    mainVm.onProfileSetupComplete(createdProfile)
                },
            )
        }

        val items = listOf(
            NavItemData(Route.Trending, "Trending", Icons.Rounded.Whatshot),
            NavItemData(Route.Search, "Search", Icons.Rounded.Search),
            NavItemData(Route.Lists, "Lists", Icons.Rounded.VideoLibrary),
            NavItemData(Route.MyReviews, "Reviews", Icons.Rounded.RateReview),
            NavItemData(Route.Settings, "Settings", Icons.Rounded.Tune),
        )

        val backStackEntry by mainNavController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        AppScaffold(
            title = items.firstOrNull { it.route.value == currentRoute }?.label ?: "CineLog",
            bottomBar = {
                CineLogModernBottomBar(
                    items = items,
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        mainNavController.navigate(targetRoute) {
                            popUpTo(mainNavController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = mainNavController,
                startDestination = Route.Trending.value,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn() + slideInHorizontally { it / 6 } },
                exitTransition = { fadeOut() + slideOutHorizontally { -it / 6 } },
                popEnterTransition = { fadeIn() + slideInHorizontally { -it / 6 } },
                popExitTransition = { fadeOut() + slideOutHorizontally { it / 6 } },
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
                        onUserClick = { userId ->
                            navController.navigate(Route.UserProfile.create(userId))
                        },
                    )
                }
                composable(Route.Settings.value) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    SettingsScreen(
                        onSignOut = {
                            authVm.signOut()
                            onSignedOut()
                        },
                        onResetPassword = {
                            authVm.sendPasswordResetForCurrentUser { _, msg ->
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                        onNavigateToProfile = { userId ->
                            navController.navigate(Route.UserProfile.create(userId))
                        },
                    )
                }
            }
        }
    }

    composable(
        route = Route.MovieDetails.value,
        arguments = listOf(navArgument(Route.MovieDetails.argMovieId) { type = NavType.LongType }),
        enterTransition = { fadeIn() + slideInHorizontally { it } },
        exitTransition = { fadeOut() + slideOutHorizontally { -it } },
        popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
        popExitTransition = { fadeOut() + slideOutHorizontally { it } },
    ) { backStackEntry ->
        val movieId = backStackEntry.arguments?.getLong(Route.MovieDetails.argMovieId) ?: return@composable
        MovieDetailsScreen(
            movieId = movieId,
            onBackClick = { navController.popBackStack() },
            onUserClick = { userId ->
                navController.navigate(Route.UserProfile.create(userId))
            },
        )
    }

    composable(
        route = Route.ListDetail.value,
        arguments = listOf(navArgument(Route.ListDetail.argListId) { type = NavType.StringType }),
        enterTransition = { fadeIn() + slideInHorizontally { it } },
        exitTransition = { fadeOut() + slideOutHorizontally { -it } },
        popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
        popExitTransition = { fadeOut() + slideOutHorizontally { it } },
    ) {
        ListDetailScreen(
            onMovieClick = { movieId ->
                navController.navigate(Route.MovieDetails.create(movieId))
            },
            onBackClick = { navController.popBackStack() },
        )
    }

    composable(
        route = Route.UserProfile.value,
        arguments = listOf(navArgument(Route.UserProfile.argUserId) { type = NavType.StringType }),
        enterTransition = { fadeIn() + slideInHorizontally { it } },
        exitTransition = { fadeOut() + slideOutHorizontally { -it } },
        popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
        popExitTransition = { fadeOut() + slideOutHorizontally { it } },
    ) {
        UserProfileScreen(
            onBack = { navController.popBackStack() },
            onMovieClick = { movieId ->
                navController.navigate(Route.MovieDetails.create(movieId))
            },
            onUserClick = { clickedUserId ->
                navController.navigate(Route.UserProfile.create(clickedUserId))
            },
        )
    }
}

@Composable
private fun CineLogModernBottomBar(
    items: List<NavItemData>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = CinemaAmber.copy(alpha = 0.25f),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            CinemaAmber.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            CinemaAmber.copy(alpha = 0.35f),
                        ),
                    ),
                    shape = RoundedCornerShape(28.dp),
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route.value
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.08f else 1f,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
                        label = "bottom_nav_scale",
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selected) {
                                    CinemaAmber.copy(alpha = 0.15f)
                                } else {
                                    Color.Transparent
                                },
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onNavigate(item.route.value) },
                            )
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) {
                                    CinemaAmber
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                },
                                modifier = Modifier.size(22.dp),
                            )

                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.5.sp,
                                    letterSpacing = 0.2.sp,
                                ),
                                color = if (selected) {
                                    CinemaAmber
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                },
                            )

                            // Active Micro-Indicator Dot
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) CinemaAmber else Color.Transparent,
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}
