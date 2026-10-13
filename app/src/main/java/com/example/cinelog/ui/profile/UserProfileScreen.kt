package com.example.cinelog.ui.profile

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.widget.Toast
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cinelog.profile.ImageUtils
import com.example.cinelog.profile.ProfileCropDialog
import com.example.cinelog.ui.components.CineLogGradientBackground
import com.example.cinelog.ui.components.SocialReviewCard
import com.example.cinelog.ui.components.UserAvatar
import com.example.cinelog.ui.theme.CinemaAmber
import com.example.cinelog.ui.theme.CinemaBlue
import com.example.cinelog.ui.theme.CinemaNavy

@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onUserClick: (String) -> Unit,
    onMessageClick: ((String) -> Unit)? = null,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val state by viewModel.uiState.collectAsState()

    var showEditBioDialog by remember { mutableStateOf(false) }
    var bioInput by remember { mutableStateOf("") }
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

    CineLogGradientBackground {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CinemaAmber)
            }
            return@CineLogGradientBackground
        }

        val profile = state.profile

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp),
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = if (state.isCurrentUser) "My Profile" else "User Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Profile Header Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        UserAvatar(
                            photoBase64 = profile?.photoBase64,
                            displayName = profile?.displayName.orEmpty(),
                            username = profile?.username.orEmpty(),
                            size = 100.dp,
                            borderWidth = 3.dp,
                        )

                        if (state.isCurrentUser) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(CinemaAmber)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    .clickable { photoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = "Change photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = profile?.displayName?.ifBlank { "CineLog Member" } ?: "CineLog Member",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                    )

                    Spacer(Modifier.height(4.dp))

                    // Unique Name Badge with Copy Button
                    val usernameText = if (profile?.username.isNullOrBlank()) "@critic" else "@${profile?.username}"
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CinemaBlue.copy(alpha = 0.16f))
                            .border(1.dp, CinemaBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable {
                                if (!profile?.username.isNullOrBlank()) {
                                    clipboardManager.setText(AnnotatedString(usernameText))
                                    Toast.makeText(context, "Unique name $usernameText copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Unique Name",
                            tint = CinemaBlue,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = usernameText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CinemaBlue,
                            ),
                        )
                        IconButton(
                            onClick = {
                                if (!profile?.username.isNullOrBlank()) {
                                    clipboardManager.setText(AnnotatedString(usernameText))
                                    Toast.makeText(context, "Unique name $usernameText copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy Unique Name",
                                tint = CinemaBlue.copy(alpha = 0.85f),
                                modifier = Modifier.size(13.dp),
                            )
                        }
                    }

                    // Private 10-character Unique ID (ONLY visible to the account owner)
                    if (state.isCurrentUser && !profile?.uniqueId.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaBlue.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(profile.uniqueId))
                                        Toast.makeText(context, "Unique ID ${profile.uniqueId} copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(CinemaNavy.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Key,
                                        contentDescription = "Unique ID",
                                        tint = CinemaBlue,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = "CineLog ID:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = profile.uniqueId,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace,
                                                letterSpacing = 1.sp,
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                    Text(
                                        text = "Private to you • Others connect using this ID or name",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(profile.uniqueId))
                                        Toast.makeText(context, "Unique ID ${profile.uniqueId} copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy ID",
                                        tint = CinemaBlue,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Bio
                    if (!profile?.bio.isNullOrBlank()) {
                        Text(
                            text = profile?.bio.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp, end = 20.dp),
                        )
                    } else if (state.isCurrentUser) {
                        Text(
                            text = "+ Add a bio to your profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaAmber,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable {
                                    bioInput = profile?.bio.orEmpty()
                                    showEditBioDialog = true
                                },
                        )
                    }

                    if (state.isCurrentUser && !profile?.bio.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clickable {
                                    bioInput = profile?.bio.orEmpty()
                                    showEditBioDialog = true
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = CinemaAmber,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = "Edit Bio",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaAmber,
                            )
                        }
                    }

                    // Follow / Following & Message Action Buttons (for other users)
                    if (!state.isCurrentUser && profile != null) {
                        Spacer(Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                onClick = { viewModel.toggleFollow() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isFollowing) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        CinemaAmber
                                    },
                                    contentColor = if (state.isFollowing) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        Color.White
                                    },
                                ),
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Icon(
                                    imageVector = if (state.isFollowing) Icons.Rounded.Check else Icons.Rounded.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (state.isFollowing) "Following" else "Follow",
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            if (onMessageClick != null) {
                                Button(
                                    onClick = { onMessageClick(profile.userId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CinemaNavy,
                                        contentColor = Color.White,
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Chat,
                                        contentDescription = "Message",
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Message",
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Social & Cinema Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Person,
                            value = "${state.followStats.followersCount}",
                            label = "Followers",
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.PersonAdd,
                            value = "${state.followStats.followingCount}",
                            label = "Following",
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Movie,
                            value = "${state.stats.moviesRatedCount}",
                            label = "Rated",
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Favorite,
                            value = "${state.stats.totalLikesReceived}",
                            label = "Likes",
                        )
                    }
                }
            }

            // Reviews Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (state.isCurrentUser) "My Reviews & Ratings" else "Reviews by ${profile?.displayName ?: "User"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${state.reviews.size} total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.reviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No movie reviews posted yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(state.reviews, key = { it.id }) { review ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        SocialReviewCard(
                            review = review,
                            currentUserId = viewModel.currentUserId,
                            onUserClick = onUserClick,
                            onMovieClick = onMovieClick,
                            onToggleLike = viewModel::toggleLike,
                            onToggleDislike = viewModel::toggleDislike,
                            commentsFlow = viewModel.commentsForReview(review.id),
                            onAddComment = viewModel::addComment,
                            onEditReview = viewModel::updateReview,
                            onDeleteReview = viewModel::deleteReview,
                        )
                    }
                }
            }
        }
    }

    // Edit Bio Dialog
    if (showEditBioDialog) {
        AlertDialog(
            onDismissRequest = { showEditBioDialog = false },
            title = { Text("Edit Bio", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = bioInput,
                    onValueChange = { bioInput = it },
                    placeholder = { Text("Write something about your movie taste...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaAmber,
                    ),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditBioDialog = false
                        viewModel.updateBio(bioInput)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaAmber,
                        contentColor = Color.White,
                    ),
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditBioDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Photo Crop Dialog
    if (showCropDialog && selectedBitmapForCrop != null) {
        ProfileCropDialog(
            bitmap = selectedBitmapForCrop!!,
            onDismiss = { showCropDialog = false },
            onCropConfirmed = { croppedBase64 ->
                showCropDialog = false
                viewModel.updateProfilePhoto(croppedBase64)
            },
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CinemaAmber,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
