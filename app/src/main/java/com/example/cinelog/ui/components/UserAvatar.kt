package com.example.cinelog.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinelog.ui.theme.CinemaAmber

@Composable
fun UserAvatar(
    photoBase64: String?,
    displayName: String,
    username: String = "",
    size: Dp = 44.dp,
    borderWidth: Dp = 1.5.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(photoBase64) {
        if (photoBase64.isNullOrBlank()) null else {
            runCatching {
                val pureBase64 = photoBase64.substringAfter("base64,")
                val decoded = Base64.decode(pureBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            }.getOrNull()
        }
    }

    val initials = remember(displayName, username) {
        val name = displayName.trim().ifBlank { username.trim() }
        val parts = name.split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        } else if (name.isNotEmpty()) {
            name.take(2).uppercase()
        } else {
            "CL"
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, CinemaAmber.copy(alpha = 0.8f), CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "$displayName Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF3B2605),
                                CinemaAmber.copy(alpha = 0.5f),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.36f).sp,
                )
            }
        }
    }
}
