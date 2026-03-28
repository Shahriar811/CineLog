package com.example.cinelog.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import com.example.cinelog.ui.components.MoviePoster

@Composable
fun MovieDetailsScreen(
    movieId: Long,
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
            title = { Text("New list") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val n = newListName.trim()
                        if (n.isNotEmpty()) {
                            viewModel.createListAndAdd(n)
                            newListName = ""
                            showCreateListDialog = false
                        }
                    },
                ) { Text("Create & add") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateListDialog = false }) { Text("Cancel") }
            },
        )
    }

    when {
        state.isLoading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        state.error != null && state.details == null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.error ?: "Failed to load",
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = { viewModel.load(movieId) }) {
                    Text("Try again")
                }
            }
        }
        state.details != null -> {
            val details = state.details!!
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.error?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(details.title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = listOfNotNull(
                        details.releaseDate,
                        details.runtimeMinutes?.let { "${it}m" },
                        details.voteAverage?.let { "★ ${"%.1f".format(it)}" },
                    ).joinToString("  •  "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                MoviePoster(
                    posterPath = details.posterPath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                )
                if (details.genres.isNotEmpty()) {
                    Text(
                        text = details.genres.joinToString(" • "),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (details.overview.isNotBlank()) {
                    Text(details.overview, style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(4.dp))
                Box {
                    FilledTonalButton(
                        onClick = { viewModel.setListMenuExpanded(true) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add to my list")
                        }
                    }
                    DropdownMenu(
                        expanded = state.listMenuExpanded,
                        onDismissRequest = { viewModel.setListMenuExpanded(false) },
                    ) {
                        state.userLists.forEach { list ->
                            DropdownMenuItem(
                                text = { Text(list.name) },
                                onClick = { viewModel.addMovieToList(list.id) },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Create new list…") },
                            onClick = {
                                viewModel.setListMenuExpanded(false)
                                showCreateListDialog = true
                            },
                        )
                    }
                }
                state.listMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(3500)
                        viewModel.clearListMessage()
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Write a review", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Rating: ${(state.ratingOutOf10?.toString() ?: "—")}/10",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    RatingRow(
                        start = 1,
                        end = 5,
                        selected = state.ratingOutOf10,
                        onRate = viewModel::setRating,
                    )
                    RatingRow(
                        start = 6,
                        end = 10,
                        selected = state.ratingOutOf10,
                        onRate = viewModel::setRating,
                    )
                }
                OutlinedTextField(
                    value = state.reviewText,
                    onValueChange = viewModel::setReviewText,
                    minLines = 4,
                    label = { Text("Write your review") },
                    modifier = Modifier.fillMaxWidth(),
                )
                FilterChip(
                    selected = state.saveOnline,
                    onClick = { viewModel.setSaveOnline(!state.saveOnline) },
                    label = { Text("Save online") },
                )
                Button(
                    onClick = viewModel::saveReview,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Submit review")
                }
                state.saveMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text("User reviews", style = MaterialTheme.typography.titleMedium)
                if (state.movieReviews.isEmpty()) {
                    Text(
                        text = "No reviews yet. Be the first one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.movieReviews.forEach { review ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                        ) {
                            Text(
                                text = "${review.ratingOutOf10}/10",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            if (review.reviewText.isNotBlank()) {
                                Text(review.reviewText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingRow(
    start: Int,
    end: Int,
    selected: Int?,
    onRate: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        (start..end).forEach { index ->
            val filled = selected != null && index <= selected
            IconButton(onClick = { onRate(index) }) {
                Icon(
                    imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Rate $index",
                    tint = if (filled) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
