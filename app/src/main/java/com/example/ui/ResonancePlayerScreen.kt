package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrackItem
import com.example.ui.components.AudioSettingsSheet
import com.example.ui.components.EqualizerSheet
import com.example.ui.components.FormatBadgePill
import com.example.ui.components.GestureAlbumArt
import com.example.ui.components.SignalPathSheet
import com.example.ui.components.SpectrumVisualizer
import com.example.ui.components.TrackQueueSheet
import com.example.ui.theme.AudiophileGold
import com.example.ui.theme.AudiophileGoldGlow
import com.example.ui.theme.BitPerfectGreen
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VoidBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResonancePlayerScreen(
    viewModel: AudioPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPositionMs by viewModel.playbackPositionMs.collectAsState()
    val spectrumLevels by viewModel.spectrumLevels.collectAsState()
    val pipelineStatus by viewModel.pipelineStatus.collectAsState()
    val isPureDirect by viewModel.isPureDirect.collectAsState()
    val isCrossfeedEnabled by viewModel.isCrossfeedEnabled.collectAsState()
    val dsdDecodeMode by viewModel.dsdDecodeMode.collectAsState()
    val resamplerMode by viewModel.resamplerMode.collectAsState()
    val eqBands by viewModel.eqBands.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val activeSheet by viewModel.activeSheet.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val tracks by viewModel.tracks.collectAsState()

    var isSliderDragging by remember { mutableStateOf(false) }
    var draggedSliderPositionMs by remember { mutableFloatStateOf(0f) }

    val signalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val eqSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar
            PlayerTopBar(
                endpointName = pipelineStatus.outputDeviceName,
                outputSampleRate = pipelineStatus.outputSampleRate,
                isBitPerfect = pipelineStatus.isBitPerfect,
                onOpenSignalPath = { viewModel.openSheet(PlayerSheet.SIGNAL_PATH) },
                onOpenEqualizer = { viewModel.openSheet(PlayerSheet.EQUALIZER) },
                onOpenQueue = { viewModel.openSheet(PlayerSheet.QUEUE) },
                onOpenSettings = { viewModel.openSheet(PlayerSheet.SETTINGS) }
            )

            // 2. Gesture Album Art (Hero)
            GestureAlbumArt(
                track = currentTrack,
                isPlaying = isPlaying,
                currentPositionMs = playbackPositionMs,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onNextTrack = { viewModel.nextTrack() },
                onPrevTrack = { viewModel.prevTrack() },
                onSwipeUpSignalPath = { viewModel.openSheet(PlayerSheet.SIGNAL_PATH) },
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(vertical = 4.dp)
            )

            // 3. Track Info & Audiophile Badges
            TrackDetailsSection(
                track = currentTrack,
                isBitPerfect = pipelineStatus.isBitPerfect,
                onBadgeClick = { viewModel.openSheet(PlayerSheet.SIGNAL_PATH) }
            )

            // 4. Real-time Spectrum Analyzer
            SpectrumVisualizer(
                levels = spectrumLevels,
                isPlaying = isPlaying,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 5. Progress Bar & Time
            val duration = currentTrack?.durationMs ?: 1L
            val currentPos = if (isSliderDragging) draggedSliderPositionMs.toLong() else playbackPositionMs
            val progressFraction = (currentPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progressFraction,
                    onValueChange = { fraction ->
                        isSliderDragging = true
                        draggedSliderPositionMs = fraction * duration
                    },
                    onValueChangeFinished = {
                        isSliderDragging = false
                        viewModel.seekTo(draggedSliderPositionMs.toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = AudiophileGold,
                        activeTrackColor = AudiophileGold,
                        inactiveTrackColor = SurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playback_progress_slider")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPos),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        text = "-${formatTime((duration - currentPos).coerceAtLeast(0L))}",
                        fontSize = 12.sp,
                        color = TextTertiary,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }

            // 6. Playback Controls
            PlaybackControlsRow(
                isPlaying = isPlaying,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.nextTrack() },
                onPrevious = { viewModel.prevTrack() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() }
            )

            // 7. Bottom Signal Path Quick Bar
            BottomSignalBar(
                track = currentTrack,
                pipeline = pipelineStatus,
                onClick = { viewModel.openSheet(PlayerSheet.SIGNAL_PATH) }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Modal Sheets
        when (activeSheet) {
            PlayerSheet.SIGNAL_PATH -> {
                SignalPathSheet(
                    track = currentTrack,
                    pipeline = pipelineStatus,
                    onDismiss = { viewModel.closeSheet() },
                    sheetState = signalSheetState
                )
            }
            PlayerSheet.EQUALIZER -> {
                EqualizerSheet(
                    isPureDirect = isPureDirect,
                    isCrossfeedEnabled = isCrossfeedEnabled,
                    eqBands = eqBands,
                    onTogglePureDirect = { viewModel.togglePureDirect() },
                    onToggleCrossfeed = { viewModel.toggleCrossfeed() },
                    onBandChanged = { band, gain -> viewModel.setEqBand(band, gain) },
                    onResetEq = { viewModel.resetEq() },
                    onApplyPreset = { viewModel.applyEqPreset(it) },
                    onDismiss = { viewModel.closeSheet() },
                    sheetState = eqSheetState
                )
            }
            PlayerSheet.QUEUE -> {
                TrackQueueSheet(
                    tracks = tracks,
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { viewModel.setFilterCategory(it) },
                    onSelectTrack = {
                        viewModel.selectTrack(it)
                        viewModel.closeSheet()
                    },
                    onImportUri = { viewModel.importAudioFile(it) },
                    onDismiss = { viewModel.closeSheet() },
                    sheetState = queueSheetState
                )
            }
            PlayerSheet.SETTINGS -> {
                AudioSettingsSheet(
                    dsdMode = dsdDecodeMode,
                    resamplerMode = resamplerMode,
                    onSelectDsdMode = { viewModel.setDsdDecodeMode(it) },
                    onSelectResamplerMode = { viewModel.setResamplerMode(it) },
                    onDismiss = { viewModel.closeSheet() },
                    sheetState = settingsSheetState
                )
            }
            PlayerSheet.NONE -> {}
        }
    }
}

@Composable
fun PlayerTopBar(
    endpointName: String,
    outputSampleRate: Int,
    isBitPerfect: Boolean,
    onOpenSignalPath: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App title
        Text(
            text = "RESONANCE",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
            color = AudiophileGold
        )

        // Active Hardware HAL Endpoint Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(
                    1.dp,
                    if (isBitPerfect) BitPerfectGreen.copy(alpha = 0.5f) else SurfaceBorder,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onOpenSignalPath() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("endpoint_status_pill")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isBitPerfect) BitPerfectGreen else AudiophileGold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${outputSampleRate / 1000}kHz • ${if (isBitPerfect) "BIT-PERFECT" else "HAL"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isBitPerfect) BitPerfectGreen else TextSecondary,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Action Icons
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenEqualizer,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("open_eq_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Equalizer & DSP",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenQueue,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("open_queue_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Track Queue & Library",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("open_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Audio Engine Settings",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TrackDetailsSection(
    track: AudioTrackItem?,
    isBitPerfect: Boolean,
    onBadgeClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = track?.title ?: "Select Master Track",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${track?.artist ?: "Unknown Artist"} — ${track?.album ?: "Hi-Res Album"}",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Badge pill row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBadgeClick() }
        ) {
            track?.let {
                FormatBadgePill(item = it)
            }

            // Bit-Perfect Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isBitPerfect) BitPerfectGreen.copy(alpha = 0.15f) else SurfaceCard)
                    .border(
                        0.5.dp,
                        if (isBitPerfect) BitPerfectGreen else SurfaceBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isBitPerfect) "BIT-PERFECT" else "DSP ACTIVE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isBitPerfect) BitPerfectGreen else AudiophileGold,
                    letterSpacing = 0.5.sp
                )
            }

            // Dynamic Range Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceCard)
                    .border(0.5.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "DR${track?.dynamicRangeScore ?: 14}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun PlaybackControlsRow(
    isPlaying: Boolean,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        IconButton(
            onClick = onToggleShuffle,
            modifier = Modifier
                .size(48.dp)
                .testTag("shuffle_button")
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffle) AudiophileGold else TextTertiary
            )
        }

        // Previous
        IconButton(
            onClick = onPrevious,
            modifier = Modifier
                .size(48.dp)
                .testTag("previous_button")
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous Track",
                tint = TextPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        // Play/Pause Hero Button
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = AudiophileGoldGlow.copy(alpha = 0.3f),
                    spotColor = AudiophileGold
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AudiophileGoldGlow,
                            AudiophileGold
                        )
                    )
                )
                .clickable { onTogglePlayPause() }
                .testTag("play_pause_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = VoidBlack,
                modifier = Modifier.size(36.dp)
            )
        }

        // Next
        IconButton(
            onClick = onNext,
            modifier = Modifier
                .size(48.dp)
                .testTag("next_button")
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next Track",
                tint = TextPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        // Repeat
        IconButton(
            onClick = onToggleRepeat,
            modifier = Modifier
                .size(48.dp)
                .testTag("repeat_button")
        ) {
            Icon(
                imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                contentDescription = "Repeat",
                tint = if (repeatMode != RepeatMode.OFF) AudiophileGold else TextTertiary
            )
        }
    }
}

@Composable
fun BottomSignalBar(
    track: AudioTrackItem?,
    pipeline: com.example.model.AudioPipelineStatus,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("bottom_signal_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (pipeline.isBitPerfect) BitPerfectGreen else AudiophileGold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${pipeline.inputFormat} ${pipeline.inputBitDepth}B/${pipeline.inputSampleRate / 1000}k -> ${pipeline.outputSampleRate / 1000}k",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "SWIPE UP FOR SIGNAL PATH",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = AudiophileGold,
                letterSpacing = 1.sp
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
