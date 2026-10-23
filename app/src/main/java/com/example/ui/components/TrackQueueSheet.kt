package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AudioCategory
import com.example.model.AudioTrackItem
import com.example.ui.theme.AudiophileGold
import com.example.ui.theme.BitPerfectGreen
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackQueueSheet(
    tracks: List<AudioTrackItem>,
    currentTrack: AudioTrackItem?,
    isPlaying: Boolean,
    selectedCategory: AudioCategory?,
    onSelectCategory: (AudioCategory?) -> Unit,
    onSelectTrack: (AudioTrackItem) -> Unit,
    onImportUri: (Uri) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onImportUri(it) }
    }

    val filteredTracks = if (selectedCategory == null) {
        tracks
    } else {
        tracks.filter { it.formatType.category == selectedCategory }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidian,
        contentColor = TextPrimary,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SurfaceBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .testTag("track_queue_sheet")
        ) {
            // Header & Import Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUDIOPHILE LIBRARY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = AudiophileGold
                    )
                    Text(
                        text = "${filteredTracks.size} Hi-Res Tracks",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "audio/*",
                                "application/octet-stream"
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = AudiophileGold
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.testTag("import_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Import", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterPill(
                    label = "All Formats",
                    isSelected = selectedCategory == null,
                    onClick = { onSelectCategory(null) }
                )
                FilterPill(
                    label = "DSD (.dsf / .dff)",
                    isSelected = selectedCategory == AudioCategory.DSD,
                    onClick = { onSelectCategory(AudioCategory.DSD) }
                )
                FilterPill(
                    label = "Lossless (FLAC/ALAC/WAV)",
                    isSelected = selectedCategory == AudioCategory.LOSSLESS,
                    onClick = { onSelectCategory(AudioCategory.LOSSLESS) }
                )
                FilterPill(
                    label = "Surround (EAC3/DTS)",
                    isSelected = selectedCategory == AudioCategory.SURROUND,
                    onClick = { onSelectCategory(AudioCategory.SURROUND) }
                )
                FilterPill(
                    label = "Standard (MP3/AAC)",
                    isSelected = selectedCategory == AudioCategory.STANDARD,
                    onClick = { onSelectCategory(AudioCategory.STANDARD) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Track list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTracks, key = { it.id }) { item ->
                    val isCurrent = item.id == currentTrack?.id
                    TrackItemRow(
                        item = item,
                        isCurrent = isCurrent,
                        isPlaying = isPlaying && isCurrent,
                        onClick = { onSelectTrack(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AudiophileGold else SurfaceCard)
            .border(
                1.dp,
                if (isSelected) AudiophileGold else SurfaceBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) DeepObsidian else TextPrimary
        )
    }
}

@Composable
fun TrackItemRow(
    item: AudioTrackItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrent) SurfaceElevated else SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrent) AudiophileGold.copy(alpha = 0.5f) else SurfaceBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("track_row_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            val artRes = item.albumArtResId ?: R.drawable.album_art_audiophile_1792750033083
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = artRes),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                    contentScale = ContentScale.Crop
                )
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Playing",
                            tint = AudiophileGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCurrent) AudiophileGold else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.artist} • ${item.album}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Badge
                FormatBadgePill(item = item)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Duration
            val min = item.durationMs / 60000
            val sec = (item.durationMs % 60000) / 1000
            Text(
                text = String.format("%d:%02d", min, sec),
                fontSize = 12.sp,
                color = TextTertiary
            )
        }
    }
}

@Composable
fun FormatBadgePill(item: AudioTrackItem) {
    val badgeColor = when {
        item.formatType.isDsd -> AudiophileGold
        item.sampleRate >= 96000 -> ElectricCyan
        item.formatType.isSurround -> BitPerfectGreen
        else -> TextTertiary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeColor.copy(alpha = 0.12f))
            .border(0.5.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = item.badgeLabel,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = badgeColor,
            letterSpacing = 0.5.sp
        )
    }
}
