package com.example.cinelog.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.cinelog.auth.AuthViewModel
import com.example.cinelog.ui.navigation.Route

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isSignedIn by authViewModel.isSignedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isSignedIn) Route.Main.value else Route.Auth.value,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() },
    ) {
        authGraph(
            navController = navController,
            onSignedIn = {
                navController.navigate(Route.Main.value) {
                    popUpTo(Route.Auth.value) { inclusive = true }
                }
            },
        )
        mainGraph(
            navController = navController,
            onSignedOut = {
                navController.navigate(Route.Auth.value) {
                    popUpTo(Route.Main.value) { inclusive = true }
                }
            },
        )
    }
}
