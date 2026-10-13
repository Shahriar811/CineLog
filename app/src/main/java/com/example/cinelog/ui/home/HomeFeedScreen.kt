package com.example.cinelog.ui.home

import android.content.Intent
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DynamicFeed
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LocalMovies
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cinelog.profile.UserProfile
import com.example.cinelog.reviews.MovieReview
import com.example.cinelog.ui.components.CineLogGradientBackground
import com.example.cinelog.ui.components.SocialReviewCard
import com.example.cinelog.ui.components.UserAvatar
import com.example.cinelog.ui.theme.CinemaAmber

@Composable
fun HomeFeedScreen(
    onMovieClick: (Long) -> Unit,
    onUserClick: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToConversations: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeFeedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    CineLogGradientBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            // Community Top Header
            HomeTopHeader(
                currentUserId = viewModel.currentUserId,
                onUserClick = onUserClick,
                isSearchActive = state.isSearchActive,
                onToggleSearch = { viewModel.setSearchActive(!state.isSearchActive) },
                onNavigateToConversations = onNavigateToConversations,
            )

            Spacer(Modifier.height(12.dp))

            // Search Bar for Cinephiles (when active)
            AnimatedVisibility(visible = state.isSearchActive) {
                Column {
                    CinephileSearchBar(
                        query = state.searchQuery,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onClear = { viewModel.onSearchQueryChanged("") },
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            // Quick "Share Review / What did you watch?" composer card
            ReviewComposerPrompt(
                onClick = onNavigateToSearch,
            )

            Spacer(Modifier.height(14.dp))

            // Feed Tabs (For You / Following / Discover)
            FeedTabCapsuleRow(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.onTabSelected(it) },
            )

            Spacer(Modifier.height(12.dp))

            // Main Feed Body
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = CinemaAmber)
                }
            } else {
                when {
                    state.isSearchActive && state.searchQuery.isNotBlank() -> {
                        // User Search Results
                        CinephileSearchResults(
                            users = state.searchResults,
                            viewModel = viewModel,
                            onUserClick = onUserClick,
                        )
                    }

                    state.selectedTab == FeedTab.DISCOVER -> {
                        // Discover Cinephiles Tab
                        DiscoverCinephilesSection(
                            suggestedUsers = state.suggestedUsers,
                            viewModel = viewModel,
                            onUserClick = onUserClick,
                        )
                    }

                    state.selectedTab == FeedTab.FOLLOWING -> {
                        // Following Feed
                        if (state.feedReviews.isEmpty()) {
                            EmptyFollowingState(
                                onDiscoverClick = { viewModel.onTabSelected(FeedTab.DISCOVER) },
                            )
                        } else {
                            ReviewFeedList(
                                reviews = state.feedReviews,
                                suggestedUsers = state.suggestedUsers,
                                viewModel = viewModel,
                                onMovieClick = onMovieClick,
                                onUserClick = onUserClick,
                            )
                        }
                    }

                    else -> {
                        // For You (Community Feed)
                        if (state.communityReviews.isEmpty()) {
                            EmptyCommunityState(
                                onExploreMovies = onNavigateToSearch,
                            )
                        } else {
                            ReviewFeedList(
                                reviews = state.communityReviews,
                                suggestedUsers = state.suggestedUsers,
                                viewModel = viewModel,
                                onMovieClick = onMovieClick,
                                onUserClick = onUserClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Top Header ─────────────────────────────────────────────────────────────

@Composable
private fun HomeTopHeader(
    currentUserId: String?,
    onUserClick: (String) -> Unit,
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit,
    onNavigateToConversations: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CinemaAmber.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalMovies,
                    contentDescription = null,
                    tint = CinemaAmber,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = "CineLog",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Movie Lovers Community",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Find People Button
            IconButton(
                onClick = onToggleSearch,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSearchActive) CinemaAmber.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
            ) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Rounded.Clear else Icons.Rounded.PersonAdd,
                    contentDescription = "Search Friends",
                    tint = if (isSearchActive) CinemaAmber else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(19.dp),
                )
            }

            // Direct Messages Button
            IconButton(
                onClick = onNavigateToConversations,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChatBubbleOutline,
                    contentDescription = "Messages",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(19.dp),
                )
            }

            // Current User Avatar
            if (currentUserId != null) {
                Box(
                    modifier = Modifier
                        .clickable { onUserClick(currentUserId) },
                ) {
                    UserAvatar(
                        photoBase64 = null,
                        displayName = "Me",
                        size = 38.dp,
                    )
                }
            }
        }
    }
}

// ── Search Bar for Finding Users ───────────────────────────────────────────

@Composable
private fun CinephileSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                text = "Search cinephiles by @username or name...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = CinemaAmber,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = "Clear",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CinemaAmber,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        ),
    )
}

// ── Quick Review Composer Prompt ───────────────────────────────────────────

@Composable
private fun ReviewComposerPrompt(
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CinemaAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RateReview,
                        contentDescription = null,
                        tint = CinemaAmber,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = "What did you watch? Share a review...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CinemaAmber)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "Rate",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

// ── Feed Tab Capsule Row ───────────────────────────────────────────────────

@Composable
private fun FeedTabCapsuleRow(
    selectedTab: FeedTab,
    onTabSelected: (FeedTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                RoundedCornerShape(20.dp),
            )
            .padding(4.dp),
    ) {
        FeedTabItem(
            label = "For You",
            icon = Icons.Rounded.DynamicFeed,
            isSelected = selectedTab == FeedTab.FOR_YOU,
            onClick = { onTabSelected(FeedTab.FOR_YOU) },
            modifier = Modifier.weight(1f),
        )
        FeedTabItem(
            label = "Following",
            icon = Icons.Rounded.Group,
            isSelected = selectedTab == FeedTab.FOLLOWING,
            onClick = { onTabSelected(FeedTab.FOLLOWING) },
            modifier = Modifier.weight(1f),
        )
        FeedTabItem(
            label = "Discover",
            icon = Icons.Rounded.Explore,
            isSelected = selectedTab == FeedTab.DISCOVER,
            onClick = { onTabSelected(FeedTab.DISCOVER) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FeedTabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) CinemaAmber.copy(alpha = 0.22f)
                else Color.Transparent
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) CinemaAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 13.sp,
                ),
                color = if (isSelected) CinemaAmber else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Review Feed List ───────────────────────────────────────────────────────

@Composable
private fun ReviewFeedList(
    reviews: List<MovieReview>,
    suggestedUsers: List<UserProfile>,
    viewModel: HomeFeedViewModel,
    onMovieClick: (Long) -> Unit,
    onUserClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 70.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Suggested Cinephiles Horizontal Rail (shown above reviews)
        if (suggestedUsers.isNotEmpty()) {
            item {
                CinephileSuggestedRow(
                    users = suggestedUsers,
                    viewModel = viewModel,
                    onUserClick = onUserClick,
                )
            }
        }

        items(
            items = reviews,
            key = { it.id },
        ) { review ->
            SocialReviewCard(
                review = review,
                currentUserId = viewModel.currentUserId,
                onUserClick = onUserClick,
                onMovieClick = onMovieClick,
                onToggleLike = { viewModel.toggleLike(it) },
                onToggleDislike = { viewModel.toggleDislike(it) },
                commentsFlow = viewModel.commentsForReview(review.id),
                onAddComment = { reviewId, text -> viewModel.addComment(reviewId, text) },
                onEditReview = { reviewId, newRating, newText ->
                    viewModel.updateReview(reviewId, newRating, newText)
                },
                onDeleteReview = { reviewId ->
                    viewModel.deleteReview(reviewId)
                },
            )
        }
    }
}

// ── Suggested Cinephiles Horizontal Row ────────────────────────────────────

@Composable
private fun CinephileSuggestedRow(
    users: List<UserProfile>,
    viewModel: HomeFeedViewModel,
    onUserClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Cinephiles to Connect With",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${users.size} members",
                style = MaterialTheme.typography.labelSmall,
                color = CinemaAmber,
            )
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(users, key = { it.userId }) { user ->
                val isFollowing by viewModel.isFollowing(user.userId).collectAsState(initial = false)
                MiniCinephileCard(
                    user = user,
                    isFollowing = isFollowing,
                    onFollowClick = { viewModel.toggleFollow(user.userId) },
                    onUserClick = { onUserClick(user.userId) },
                )
            }
        }
    }
}

@Composable
private fun MiniCinephileCard(
    user: UserProfile,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onUserClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onUserClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UserAvatar(
                photoBase64 = user.photoBase64,
                displayName = user.displayName,
                username = user.username,
                size = 46.dp,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = user.displayName.ifBlank { "@${user.username}" },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.labelSmall,
                color = CinemaAmber,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else CinemaAmber,
                    contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
            ) {
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ── Discover Cinephiles Section ────────────────────────────────────────────

@Composable
private fun DiscoverCinephilesSection(
    suggestedUsers: List<UserProfile>,
    viewModel: HomeFeedViewModel,
    onUserClick: (String) -> Unit,
) {
    if (suggestedUsers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(54.dp),
                )
                Text(
                    text = "No other users found yet.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Invite friends to CineLog and connect your film circles!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 70.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = "Recommended Film Critics & Friends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            items(suggestedUsers, key = { it.userId }) { user ->
                val isFollowing by viewModel.isFollowing(user.userId).collectAsState(initial = false)
                CinephileListItem(
                    user = user,
                    isFollowing = isFollowing,
                    onFollowClick = { viewModel.toggleFollow(user.userId) },
                    onUserClick = { onUserClick(user.userId) },
                )
            }
        }
    }
}

// ── Cinephile Search Results ───────────────────────────────────────────────

@Composable
private fun CinephileSearchResults(
    users: List<UserProfile>,
    viewModel: HomeFeedViewModel,
    onUserClick: (String) -> Unit,
) {
    if (users.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "No film lovers matched your search",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 70.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(users, key = { it.userId }) { user ->
                val isFollowing by viewModel.isFollowing(user.userId).collectAsState(initial = false)
                CinephileListItem(
                    user = user,
                    isFollowing = isFollowing,
                    onFollowClick = { viewModel.toggleFollow(user.userId) },
                    onUserClick = { onUserClick(user.userId) },
                )
            }
        }
    }
}

// ── Cinephile Full Row Item ────────────────────────────────────────────────

@Composable
private fun CinephileListItem(
    user: UserProfile,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onUserClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onUserClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UserAvatar(
                    photoBase64 = user.photoBase64,
                    displayName = user.displayName,
                    username = user.username,
                    size = 48.dp,
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName.ifBlank { "@${user.username}" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaAmber,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (user.bio.isNotBlank()) {
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else CinemaAmber,
                    contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.White,
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Icon(
                    imageVector = if (isFollowing) Icons.Rounded.Check else Icons.Rounded.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

// ── Empty States ───────────────────────────────────────────────────────────

@Composable
private fun EmptyFollowingState(
    onDiscoverClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(CinemaAmber.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Group,
                    contentDescription = null,
                    tint = CinemaAmber,
                    modifier = Modifier.size(34.dp),
                )
            }

            Text(
                text = "Your Following Feed is Quiet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "You are not following any movie lovers yet! Discover fellow cinephiles to see their reviews, ratings, and recommendations right here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onDiscoverClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaAmber,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Discover Cinephiles",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun EmptyCommunityState(
    onExploreMovies: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(CinemaAmber.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Movie,
                    contentDescription = null,
                    tint = CinemaAmber,
                    modifier = Modifier.size(34.dp),
                )
            }

            Text(
                text = "Welcome to CineLog Community!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "No reviews have been posted yet. Be the first critic to rate and review your favorite movies with fellow cinephiles!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onExploreMovies,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaAmber,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.RateReview,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Find a Movie to Review",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
