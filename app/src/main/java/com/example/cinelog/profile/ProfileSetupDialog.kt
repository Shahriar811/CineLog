package com.example.cinelog.profile

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinelog.ui.components.UserAvatar
import com.example.cinelog.ui.theme.CinemaAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileSetupDialog(
    initialDisplayName: String,
    profileRepository: ProfileRepository,
    onProfileCreated: (UserProfile) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var usernameInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf(initialDisplayName) }
    var bioInput by remember { mutableStateOf("") }
    var photoBase64 by remember { mutableStateOf<String?>(null) }

    var isCheckingAvailability by remember { mutableStateOf(false) }
    var isUsernameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var submissionError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    var selectedBitmapForCrop by remember { mutableStateOf<Bitmap?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            val bitmap = ImageUtils.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                selectedBitmapForCrop = bitmap
                showCropDialog = true
            }
        }
    }

    // Debounced username availability check
    LaunchedEffect(usernameInput) {
        val clean = usernameInput.trim().lowercase().removePrefix("@")
        if (clean.isBlank()) {
            isUsernameAvailable = null
            usernameError = null
            return@LaunchedEffect
        }
        if (!clean.matches(Regex("^[a-z0-9_]{3,20}$"))) {
            isUsernameAvailable = false
            usernameError = "3-20 characters using letters, numbers & _"
            return@LaunchedEffect
        }

        isCheckingAvailability = true
        usernameError = null
        delay(400)
        val available = profileRepository.isUsernameAvailable(clean)
        isCheckingAvailability = false
        isUsernameAvailable = available
        if (!available) {
            usernameError = "This username is already taken."
        }
    }

    AlertDialog(
        onDismissRequest = { /* Modal required for new users */ },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(CinemaAmber.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Badge,
                    contentDescription = null,
                    tint = CinemaAmber,
                    modifier = Modifier.size(30.dp),
                )
            }
        },
        title = {
            Text(
                text = "Claim Your CineLog Identity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Welcome to CineLog! Choose your unique username. It is permanent and cannot be changed once claimed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Avatar preview & pick button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        UserAvatar(
                            photoBase64 = photoBase64,
                            displayName = displayNameInput,
                            username = usernameInput,
                            size = 64.dp,
                            borderWidth = 2.dp,
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(CinemaAmber),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddAPhoto,
                                contentDescription = "Add photo",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Profile Avatar",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Tap to choose & crop photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Unique Username Field
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = {
                        usernameInput = it.lowercase().trim().removePrefix("@")
                    },
                    label = { Text("Unique Username") },
                    placeholder = { Text("cine_critic") },
                    prefix = { Text("@", color = CinemaAmber, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    isError = isUsernameAvailable == false,
                    trailingIcon = {
                        if (isCheckingAvailability) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = CinemaAmber,
                            )
                        } else if (isUsernameAvailable == true) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Available",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp),
                            )
                        } else if (isUsernameAvailable == false) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = "Unavailable",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    supportingText = {
                        if (usernameError != null) {
                            Text(usernameError!!, color = MaterialTheme.colorScheme.error)
                        } else if (isUsernameAvailable == true) {
                            Text("Username is available!", color = Color(0xFF4CAF50))
                        } else {
                            Text("Permanent handle. 3-20 letters/numbers.")
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaAmber,
                    ),
                )

                // Display Name Field
                OutlinedTextField(
                    value = displayNameInput,
                    onValueChange = { displayNameInput = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("Your Name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = CinemaAmber,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaAmber,
                    ),
                )

                if (submissionError != null) {
                    Text(
                        text = submissionError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isUsernameAvailable != true) return@Button
                    isSubmitting = true
                    submissionError = null
                    coroutineScope.launch {
                        val result = profileRepository.claimUsernameAndSetupProfile(
                            username = usernameInput,
                            displayName = displayNameInput,
                            photoBase64 = photoBase64,
                            bio = bioInput,
                        )
                        isSubmitting = false
                        result.onSuccess { profile ->
                            onProfileCreated(profile)
                        }.onFailure { err ->
                            submissionError = err.message ?: "Failed to claim username. Please try again."
                        }
                    }
                },
                enabled = isUsernameAvailable == true && !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaAmber,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Text("Claim Identity & Enter CineLog", fontWeight = FontWeight.Bold)
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
    )

    if (showCropDialog && selectedBitmapForCrop != null) {
        ProfileCropDialog(
            bitmap = selectedBitmapForCrop!!,
            onDismiss = { showCropDialog = false },
            onCropConfirmed = { croppedBase64 ->
                photoBase64 = croppedBase64
                showCropDialog = false
            },
        )
    }
}
