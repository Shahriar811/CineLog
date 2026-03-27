package com.example.cinelog.ui.myreviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MyReviewsScreen(
    onMovieClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyReviewsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    if (state.error != null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(state.error ?: "Unable to load reviews", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    if (state.items.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("My Reviews", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "No reviews yet. Open any movie and submit your rating + review.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "My Reviews",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            )
        }
        items(state.items, key = { it.id }) { review ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = { onMovieClick(review.movieId) },
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(review.movieTitle, style = MaterialTheme.typography.titleMedium)
                    Text("${review.ratingOutOf10}/10", style = MaterialTheme.typography.labelLarge)
                    if (review.reviewText.isNotBlank()) {
                        Text(
                            text = review.reviewText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

