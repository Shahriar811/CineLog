package com.example.cinelog.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinelog.auth.AuthUiState
import com.example.cinelog.ui.components.CineLogGradientBackground
import com.example.cinelog.ui.theme.CinemaAmber
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AuthScreen(
    state: StateFlow<AuthUiState>,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onSignUpWithGmail: (String, String) -> Unit = { _, _ -> },
    onGoogleSignIn: () -> Unit,
    onSendPasswordReset: (String?) -> Unit,
) {
    val uiState by state.collectAsState()
    var isSignUpMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }
    var showOpenWithGmailDialog by remember { mutableStateOf(false) }
    var gmailAddressInput by remember { mutableStateOf("") }
    var gmailPasswordInput by remember { mutableStateOf("") }
    var gmailPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    CineLogGradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(20.dp))

                // Brand Emblem
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(16.dp, CircleShape)
                        .clip(CircleShape)
                        .background(CinemaAmber)
                        .border(2.dp, Color(0xFF60A5FA), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MovieFilter,
                        contentDescription = "CineLog Emblem",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp),
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "CineLog",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "Your personal cinema review journal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )

                Spacer(Modifier.height(26.dp))

                // Mode Selector Capsule (Sign In vs Create Account)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(20.dp),
                        )
                        .padding(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (!isSignUpMode) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6)),
                                    )
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                },
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { isSignUpMode = false },
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (!isSignUpMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSignUpMode) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6)),
                                    )
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                },
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { isSignUpMode = true },
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSignUpMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Credentials Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Email Field
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = onEmailChanged,
                            label = { Text(if (isSignUpMode) "Gmail or Email address" else "Email address") },
                            placeholder = { Text(if (isSignUpMode) "you@gmail.com" else "you@example.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.AlternateEmail,
                                    contentDescription = null,
                                    tint = CinemaAmber,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CinemaAmber,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            ),
                        )

                        // Quick @gmail.com completion suggestion in create mode
                        if (isSignUpMode && uiState.email.isNotBlank() && !uiState.email.contains("@")) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CinemaAmber.copy(alpha = 0.15f))
                                        .clickable {
                                            onEmailChanged("${uiState.email.trim()}@gmail.com")
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = "+ @gmail.com",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CinemaAmber,
                                    )
                                }
                            }
                        }

                        // Password Field
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = onPasswordChanged,
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = CinemaAmber,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (isSignUpMode) onSignUp() else onSignIn()
                                },
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CinemaAmber,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            ),
                        )

                        // Status Banner (Success or Error)
                        AnimatedVisibility(
                            visible = uiState.message != null,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            uiState.message?.let { msg ->
                                val isSuccess = uiState.isSuccessMessage
                                val bannerBg = if (isSuccess) Color(0xFF13361E) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                val bannerTint = if (isSuccess) Color(0xFF81C784) else MaterialTheme.colorScheme.error
                                val bannerIcon = if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bannerBg)
                                        .border(
                                            1.dp,
                                            bannerTint.copy(alpha = 0.35f),
                                            RoundedCornerShape(12.dp),
                                        )
                                        .padding(12.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = bannerIcon,
                                            contentDescription = null,
                                            tint = bannerTint,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(
                                            text = msg,
                                            color = bannerTint,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }
                            }
                        }

                        // Primary Action Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = CinemaAmber.copy(alpha = 0.35f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (!uiState.isLoading) {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6)),
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(CinemaAmber.copy(alpha = 0.5f), CinemaAmber.copy(alpha = 0.5f)),
                                        )
                                    },
                                )
                                .clickable(
                                    enabled = !uiState.isLoading,
                                    onClick = {
                                        focusManager.clearFocus()
                                        if (isSignUpMode) onSignUp() else onSignIn()
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color.White,
                                )
                            } else {
                                Text(
                                    text = if (isSignUpMode) "Create Account with Gmail / Email" else "Sign In",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                )
                            }
                        }

                        if (!isSignUpMode) {
                            TextButton(
                                onClick = {
                                    resetEmailInput = uiState.email
                                    showResetPasswordDialog = true
                                },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LockReset,
                                        contentDescription = null,
                                        tint = CinemaAmber,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "Forgot password?",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = CinemaAmber,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    )
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Google Sign In Button
                OutlinedButton(
                    onClick = onGoogleSignIn,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = CinemaAmber,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (isSignUpMode) "Sign up with Google" else "Continue with Google",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Direct Open Account with Gmail Button
                OutlinedButton(
                    onClick = {
                        gmailAddressInput = if (uiState.email.isNotBlank()) uiState.email else ""
                        showOpenWithGmailDialog = true
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        CinemaAmber.copy(alpha = 0.5f),
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CinemaAmber.copy(alpha = 0.08f),
                        contentColor = CinemaAmber,
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AlternateEmail,
                            contentDescription = null,
                            tint = CinemaAmber,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Sign in or Open account using Gmail",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // Dedicated Direct Open Account / Sign In with Gmail Dialog
        if (showOpenWithGmailDialog) {
            AlertDialog(
                onDismissRequest = { showOpenWithGmailDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CinemaAmber.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AlternateEmail,
                            contentDescription = null,
                            tint = CinemaAmber,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                title = {
                    Text(
                        text = "Sign In or Open Account with Gmail",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Directly sign in or create your CineLog account with your Gmail address and password.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        OutlinedTextField(
                            value = gmailAddressInput,
                            onValueChange = { gmailAddressInput = it },
                            label = { Text("Gmail Address") },
                            placeholder = { Text("yourname@gmail.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.AlternateEmail,
                                    contentDescription = null,
                                    tint = CinemaAmber,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CinemaAmber,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            ),
                        )

                        OutlinedTextField(
                            value = gmailPasswordInput,
                            onValueChange = { gmailPasswordInput = it },
                            label = { Text("Password (min 6 characters)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = CinemaAmber,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { gmailPasswordVisible = !gmailPasswordVisible }) {
                                    Icon(
                                        imageVector = if (gmailPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                            visualTransformation = if (gmailPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CinemaAmber,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            ),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val emailToUse = gmailAddressInput.trim().let {
                                if (!it.contains("@")) "$it@gmail.com" else it
                            }
                            if (emailToUse.isNotBlank() && gmailPasswordInput.isNotBlank()) {
                                showOpenWithGmailDialog = false
                                onSignUpWithGmail(emailToUse, gmailPasswordInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaAmber,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Continue with Gmail", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOpenWithGmailDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(22.dp),
            )
        }

        // Dedicated Reset Password Dialog
        if (showResetPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showResetPasswordDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CinemaAmber.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LockReset,
                            contentDescription = null,
                            tint = CinemaAmber,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                title = {
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Enter your registered email address and we'll send you instructions to reset your password.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = resetEmailInput,
                            onValueChange = { resetEmailInput = it },
                            label = { Text("Email address") },
                            placeholder = { Text("you@example.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.AlternateEmail,
                                    contentDescription = null,
                                    tint = CinemaAmber,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done,
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CinemaAmber,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            ),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetPasswordDialog = false
                            onSendPasswordReset(resetEmailInput)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaAmber,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Send Reset Link", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetPasswordDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(22.dp),
            )
        }
    }
}
