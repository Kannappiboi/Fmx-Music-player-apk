package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AudioTrackItem
import com.example.ui.theme.AudiophileGold
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.VoidBlack
import kotlin.math.abs

@Composable
fun GestureAlbumArt(
    track: AudioTrackItem?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    onSwipeUpSignalPath: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDraggingHorizontally by remember { mutableStateOf(false) }
    var dragAccumulatedX by remember { mutableFloatStateOf(0f) }
    var dragAccumulatedY by remember { mutableFloatStateOf(0f) }
    var scrubTargetMs by remember { mutableLongStateOf(0L) }

    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.96f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "artScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .padding(16.dp)
            .scale(scale)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = AudiophileGold.copy(alpha = 0.2f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        AudiophileGold.copy(alpha = 0.4f),
                        DeepObsidian,
                        Color.Black
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("album_art_gesture_area")
            .pointerInput(track?.id, isPlaying, currentPositionMs) {
                detectTapGestures(
                    onTap = {
                        onTogglePlayPause()
                    }
                )
            }
            .pointerInput(track?.id, currentPositionMs) {
                detectDragGestures(
                    onDragStart = {
                        dragAccumulatedX = 0f
                        dragAccumulatedY = 0f
                        isDraggingHorizontally = false
                        scrubTargetMs = currentPositionMs
                    },
                    onDragEnd = {
                        if (isDraggingHorizontally) {
                            onSeek(scrubTargetMs)
                            isDraggingHorizontally = false
                        } else if (dragAccumulatedY < -150f) {
                            // Vertical swipe up -> Open Signal Path
                            onSwipeUpSignalPath()
                        } else if (dragAccumulatedX > 180f) {
                            // Fling right -> previous
                            onPrevTrack()
                        } else if (dragAccumulatedX < -180f) {
                            // Fling left -> next
                            onNextTrack()
                        }
                        dragAccumulatedX = 0f
                        dragAccumulatedY = 0f
                        isDraggingHorizontally = false
                    },
                    onDragCancel = {
                        isDraggingHorizontally = false
                        dragAccumulatedX = 0f
                        dragAccumulatedY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulatedX += dragAmount.x
                        dragAccumulatedY += dragAmount.y

                        // Check if horizontal scrub threshold exceeded
                        if (abs(dragAccumulatedX) > 40f && abs(dragAccumulatedX) > abs(dragAccumulatedY) * 1.5f) {
                            isDraggingHorizontally = true
                            val duration = track?.durationMs ?: 1L
                            // 1px drag = ~250ms scrub
                            val scrubDeltaMs = (dragAccumulatedX * 250).toLong()
                            scrubTargetMs = (currentPositionMs + scrubDeltaMs).coerceIn(0L, duration)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Album art image
        val artRes = track?.albumArtResId ?: R.drawable.album_art_audiophile_1792750033083
        Image(
            painter = painterResource(id = artRes),
            contentDescription = "${track?.title ?: "Music"} Album Artwork",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Subtle vinyl groove reflection overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // Scrubbing preview overlay
        AnimatedVisibility(
            visible = isDraggingHorizontally,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val deltaSeconds = (scrubTargetMs - currentPositionMs) / 1000
            val sign = if (deltaSeconds >= 0) "+" else ""
            val formattedDelta = "$sign${deltaSeconds}s"

            val targetMin = scrubTargetMs / 60000
            val targetSec = (scrubTargetMs % 60000) / 1000
            val formattedTarget = String.format("%02d:%02d", targetMin, targetSec)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(VoidBlack.copy(alpha = 0.85f))
                    .border(1.dp, AudiophileGold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$formattedTarget ($formattedDelta)",
                    color = AudiophileGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Tap hint badge when paused
        if (!isPlaying && !isDraggingHorizontally) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(VoidBlack.copy(alpha = 0.55f))
                    .padding(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Tap to Play",
                    tint = AudiophileGold,
                    modifier = Modifier.scale(1.4f)
                )
            }
        }
    }
}
