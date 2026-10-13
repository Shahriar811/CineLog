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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
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
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
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
                                    tint = Color(0xFF1E1400),
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

                    // Unique Username Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CinemaAmber.copy(alpha = 0.16f))
                            .border(1.dp, CinemaAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Unique ID",
                            tint = CinemaAmber,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = if (profile?.username.isNullOrBlank()) "@critic" else "@${profile?.username}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CinemaAmber,
                            ),
                        )
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

                    // Follow / Following Action Button (for other users)
                    if (!state.isCurrentUser && profile != null) {
                        Spacer(Modifier.height(14.dp))
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
                                    Color(0xFF1E1400)
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
                        contentColor = Color(0xFF1E1400),
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
