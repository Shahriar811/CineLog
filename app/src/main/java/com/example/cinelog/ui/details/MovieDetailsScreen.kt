package com.example.cinelog.ui.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.cinelog.ui.components.MoviePoster
import com.example.cinelog.ui.components.SocialReviewCard
import com.example.cinelog.ui.components.shimmerEffect
import com.example.cinelog.ui.theme.CinemaAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieDetailsScreen(
    movieId: Long,
    onBackClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MovieDetailsViewModel = hiltViewModel(),
) {
    LaunchedEffect(movieId) {
        viewModel.load(movieId)
    }
    val state by viewModel.state.collectAsState()
    var newListName by remember { mutableStateOf("") }
    var showCreateListDialog by remember { mutableStateOf(false) }

    if (showCreateListDialog) {
        AlertDialog(
            onDismissRequest = { showCreateListDialog = false },
            title = {
                Text(
                    "New Collection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Collection name") },
                    placeholder = { Text("e.g. Weekend Watchlist") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val n = newListName.trim()
                        if (n.isNotEmpty()) {
                            viewModel.createListAndAdd(n)
                            newListName = ""
                            showCreateListDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaAmber, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Create & Add", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateListDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(22.dp),
        )
    }

    when {
        state.isLoading -> {
            MovieDetailsSkeleton(onBackClick = onBackClick)
        }
        state.error != null && state.details == null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error ?: "Failed to load movie details",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.load(movieId) },
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text("Try again")
                    }
                }
            }
        }
        state.details != null -> {
            val details = state.details!!
            val backdropUrl = details.posterPath?.let { "https://image.tmdb.org/t/p/w1280$it" }

            Box(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Hero Backdrop & Floating Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp),
                    ) {
                        if (backdropUrl != null) {
                            AsyncImage(
                                model = backdropUrl,
                                contentDescription = details.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        // Gradient Fade
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.4f),
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.background,
                                        ),
                                    ),
                                ),
                        )
                    }

                    // Content Container (Overlapping the backdrop)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .graphicsLayer { translationY = -70f },
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Poster + Meta Overview Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            MoviePoster(
                                posterPath = details.posterPath,
                                modifier = Modifier
                                    .size(width = 110.dp, height = 165.dp),
                                shape = RoundedCornerShape(18.dp),
                                elevation = 8.dp,
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = details.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    details.voteAverage?.let { rating ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(CinemaAmber.copy(alpha = 0.16f))
                                                .padding(horizontal = 7.dp, vertical = 3.dp),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Star,
                                                    contentDescription = null,
                                                    tint = CinemaAmber,
                                                    modifier = Modifier.size(13.dp),
                                                )
                                                Text(
                                                    text = String.format("%.1f", rating),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = CinemaAmber,
                                                    ),
                                                )
                                            }
                                        }
                                    }

                                    details.runtimeMinutes?.let { minutes ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                                .padding(horizontal = 7.dp, vertical = 3.dp),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Schedule,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(12.dp),
                                                )
                                                Text(
                                                    text = "${minutes}m",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    ),
                                                )
                                            }
                                        }
                                    }

                                    details.releaseDate?.take(4)?.let { year ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                                .padding(horizontal = 7.dp, vertical = 3.dp),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CalendarToday,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(11.dp),
                                                )
                                                Text(
                                                    text = year,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Genre Capsules
                        if (details.genres.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                details.genres.forEach { genre ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(14.dp),
                                            )
                                            .padding(horizontal = 12.dp, vertical = 5.dp),
                                    ) {
                                        Text(
                                            text = genre,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            ),
                                        )
                                    }
                                }
                            }
                        }

                        // Add to Collection Button
                        Box {
                            Button(
                                onClick = { viewModel.setListMenuExpanded(true) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BookmarkAdd,
                                    contentDescription = null,
                                    tint = CinemaAmber,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Add to Collection", fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = state.listMenuExpanded,
                                onDismissRequest = { viewModel.setListMenuExpanded(false) },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            ) {
                                state.userLists.forEach { list ->
                                    DropdownMenuItem(
                                        text = { Text(list.name) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.AutoMirrored.Rounded.PlaylistAdd,
                                                contentDescription = null,
                                                tint = CinemaAmber,
                                            )
                                        },
                                        onClick = { viewModel.addMovieToList(list.id) },
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Create new list…", fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.BookmarkAdd,
                                            contentDescription = null,
                                            tint = CinemaAmber,
                                        )
                                    },
                                    onClick = {
                                        viewModel.setListMenuExpanded(false)
                                        showCreateListDialog = true
                                    },
                                )
                            }
                        }

                        state.listMessage?.let { msg ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = CinemaAmber,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = msg,
                                    color = CinemaAmber,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            LaunchedEffect(msg) {
                                kotlinx.coroutines.delay(3000)
                                viewModel.clearListMessage()
                            }
                        }

                        // Synopsis Section
                        if (details.overview.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                ),
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Synopsis",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = details.overview,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                                        lineHeight = 22.sp,
                                    )
                                }
                            }
                        }

                        // Interactive Review Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.RateReview,
                                        contentDescription = null,
                                        tint = CinemaAmber,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Text(
                                        text = "Log Your Review",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                val scoreDescriptor = when (state.ratingOutOf10) {
                                    10 -> "Masterpiece ★★★★★"
                                    9 -> "Phenomenal"
                                    8 -> "Great Watch"
                                    7 -> "Good Film"
                                    6 -> "Enjoyable"
                                    5 -> "Average"
                                    4 -> "Underwhelming"
                                    3 -> "Poor"
                                    2 -> "Awful"
                                    1 -> "Unbearable"
                                    else -> "Tap to rate"
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Your Score: ${state.ratingOutOf10?.let { "$it/10" } ?: "—"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = scoreDescriptor,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CinemaAmber,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                }

                                // Interactive Rating Star Rows
                                RatingStarSelector(
                                    selectedRating = state.ratingOutOf10,
                                    onRate = viewModel::setRating,
                                )

                                OutlinedTextField(
                                    value = state.reviewText,
                                    onValueChange = viewModel::setReviewText,
                                    minLines = 3,
                                    label = { Text("What did you think of the film?") },
                                    placeholder = { Text("Acting, cinematography, plot twists…") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CinemaAmber,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    ),
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CloudUpload,
                                            contentDescription = null,
                                            tint = if (state.saveOnline) CinemaAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(
                                            text = "Sync to Community",
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                    Switch(
                                        checked = state.saveOnline,
                                        onCheckedChange = { viewModel.setSaveOnline(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = CinemaAmber,
                                            checkedTrackColor = CinemaAmber.copy(alpha = 0.35f),
                                        ),
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = CinemaAmber.copy(alpha = 0.3f))
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6)),
                                            ),
                                        )
                                        .clickable(onClick = viewModel::saveReview),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("Post Review", fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }

                                state.saveMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        color = CinemaAmber,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }

                        // Community Reviews Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Community Reviews",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            if (state.movieReviews.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "No reviews yet. Be the first to share your thoughts!",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            } else {
                                state.movieReviews.forEach { review ->
                                    SocialReviewCard(
                                        review = review,
                                        currentUserId = viewModel.currentUserId,
                                        onUserClick = onUserClick,
                                        onMovieClick = { /* Already on this movie's detail page */ },
                                        onToggleLike = { reviewId -> viewModel.toggleLike(reviewId) },
                                        onToggleDislike = { reviewId -> viewModel.toggleDislike(reviewId) },
                                        commentsFlow = viewModel.commentsForReview(review.id),
                                        onAddComment = { reviewId, text -> viewModel.addComment(reviewId, text) },
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(30.dp))
                    }
                }

                // Floating Frosted Back Button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 14.dp, top = 8.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingStarSelector(
    selectedRating: Int?,
    onRate: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            (1..5).forEach { starIndex ->
                StarButton(index = starIndex, selected = selectedRating, onRate = onRate)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            (6..10).forEach { starIndex ->
                StarButton(index = starIndex, selected = selectedRating, onRate = onRate)
            }
        }
    }
}

@Composable
private fun StarButton(
    index: Int,
    selected: Int?,
    onRate: (Int) -> Unit,
) {
    val isFilled = selected != null && index <= selected
    val isDirectTarget = selected == index
    val scale by animateFloatAsState(
        targetValue = if (isDirectTarget) 1.25f else if (isFilled) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 450f),
        label = "star_scale",
    )

    IconButton(
        onClick = { onRate(index) },
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isDirectTarget) CinemaAmber.copy(alpha = 0.18f) else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isFilled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = "Rate $index",
                tint = if (isFilled) CinemaAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f),
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun MovieDetailsSkeleton(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .shimmerEffect(),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerEffect(),
                )
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmerEffect(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmerEffect(),
                )
            }
        }
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 14.dp, top = 8.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
            )
        }
    }
}
