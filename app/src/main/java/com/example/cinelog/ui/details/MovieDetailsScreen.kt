package com.example.cinelog.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    when {
        state.isLoading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
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
                Text(details.title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = listOfNotNull(
                        details.releaseDate,
                        details.runtimeMinutes?.let { "${it}m" },
                        details.voteAverage?.let { "★ ${"%.1f".format(it)}" },
                    ).joinToString("  •  "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Text(details.overview, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(8.dp))
                Text("Write a review", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Rating: ${state.ratingOutOf10}/10",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    (1..10).forEach { index ->
                        TextButton(
                            onClick = { viewModel.setRating(index) },
                            modifier = Modifier.padding(0.dp),
                        ) {
                            Icon(
                                imageVector = if (index <= state.ratingOutOf10) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Rate $index",
                                tint = if (index <= state.ratingOutOf10) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
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

