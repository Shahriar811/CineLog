package com.example.cinelog.ui

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.cinelog.auth.AuthViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.cinelog.ui.auth.AuthScreen
import com.example.cinelog.ui.navigation.Route

fun androidx.navigation.NavGraphBuilder.authGraph(
    navController: NavHostController,
    onSignedIn: () -> Unit,
) {
    navigation(
        startDestination = "auth_screen",
        route = Route.Auth.value,
    ) {
        composable("auth_screen") {
            val vm: AuthViewModel = hiltViewModel()
            val context = LocalContext.current
            AuthScreen(
                state = vm.state,
                onEmailChanged = vm::setEmail,
                onPasswordChanged = vm::setPassword,
                onSignIn = { vm.signInWithEmail(onSuccess = onSignedIn) },
                onSignUp = { vm.signUpWithEmail(onSuccess = onSignedIn) },
                onGoogleSignIn = {
                    val activity = context as? android.app.Activity ?: return@AuthScreen
                    vm.signInWithGoogle(activity, onSuccess = onSignedIn)
                },
                onSendPasswordReset = vm::sendPasswordReset,
            )
        }
    }
}

