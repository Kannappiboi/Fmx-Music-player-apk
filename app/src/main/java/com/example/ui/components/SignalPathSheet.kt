package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioPipelineStatus
import com.example.model.AudioTrackItem
import com.example.ui.theme.AudiophileGold
import com.example.ui.theme.BitPerfectGreen
import com.example.ui.theme.BitPerfectGreenGlow
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
fun SignalPathSheet(
    track: AudioTrackItem?,
    pipeline: AudioPipelineStatus,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
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
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
                .testTag("signal_path_sheet")
        ) {
            // Header with Bit-Perfect indicator badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUDIO SIGNAL PATH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = AudiophileGold
                    )
                    Text(
                        text = "Hardware HAL & Bitstream Telemetry",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                // Bit-perfect status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (pipeline.isBitPerfect) BitPerfectGreen.copy(alpha = 0.15f) else SurfaceCard)
                        .border(
                            1.dp,
                            if (pipeline.isBitPerfect) BitPerfectGreen else SurfaceBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (pipeline.isBitPerfect) BitPerfectGreen else AudiophileGold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (pipeline.isBitPerfect) "BIT-PERFECT DIRECT" else "ACTIVE DSP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pipeline.isBitPerfect) BitPerfectGreen else AudiophileGold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 1: Source
            SignalPathNode(
                icon = Icons.Default.MusicNote,
                title = "Source Track",
                subtitle = "${pipeline.inputFormat} • ${track?.title ?: "Master Audio"}",
                details = listOf(
                    "Format" to pipeline.inputFormat,
                    "Sample Rate" to "${pipeline.inputSampleRate} Hz",
                    "Bit Depth" to "${pipeline.inputBitDepth}-Bit",
                    "Bitrate" to "${track?.bitrateKbps ?: 0} kbps"
                ),
                accentColor = AudiophileGold
            )

            SignalPathConnector()

            // Step 2: Decoder Engine
            SignalPathNode(
                icon = Icons.Default.Memory,
                title = "Decoding Pipeline",
                subtitle = pipeline.activeDecoder,
                details = listOf(
                    "Architecture" to "Native NativeAudioTrack / FFmpeg",
                    "Engine Mode" to if (track?.formatType?.isDsd == true) "Delta-Sigma Decimation" else "Zero-Loss PCM Unpacking",
                    "Dynamic Range" to "DR${track?.dynamicRangeScore ?: 14} dB"
                ),
                accentColor = ElectricCyan
            )

            SignalPathConnector()

            // Step 3: DSP & EQ Stage
            SignalPathNode(
                icon = Icons.Default.GraphicEq,
                title = "DSP & Spatial Stage",
                subtitle = pipeline.dspProcessing,
                details = listOf(
                    "Equalizer" to if (pipeline.isBitPerfect) "Bypassed (Bit-Accurate)" else "Active (10-Band)",
                    "Crossfeed" to if (pipeline.isBitPerfect) "Disabled" else "Bauer Spatializer",
                    "Dithering" to if (pipeline.isBitPerfect) "None (Direct Output)" else "TPDF Triangular"
                ),
                accentColor = if (pipeline.isBitPerfect) BitPerfectGreen else AudiophileGold
            )

            SignalPathConnector()

            // Step 4: Android Audio HAL
            SignalPathNode(
                icon = Icons.Default.Speed,
                title = "Android Audio HAL",
                subtitle = "Direct AudioTrack (Bypass Mixer)",
                details = listOf(
                    "Native Stream" to "FLAG_HW_AV_SYNC Low-Latency",
                    "Buffer Size" to "${pipeline.bufferSizeFrames} frames",
                    "Hardware Latency" to "${pipeline.latencyMs} ms",
                    "Buffer Underruns" to "${pipeline.underrunCount}"
                ),
                accentColor = ElectricCyan
            )

            SignalPathConnector()

            // Step 5: Output Endpoint
            SignalPathNode(
                icon = if (pipeline.isUsbDacConnected) Icons.Default.Usb else Icons.Default.Headphones,
                title = "Output Endpoint",
                subtitle = pipeline.outputDeviceName,
                details = listOf(
                    "Endpoint Device" to pipeline.outputDeviceName,
                    "Output Rate" to "${pipeline.outputSampleRate} Hz",
                    "Output Depth" to "${pipeline.outputBitDepth}-Bit Float/Packed",
                    "USB UAC2 Direct" to if (pipeline.usbDirectMode) "Supported & Enabled" else "Standard HAL"
                ),
                accentColor = if (pipeline.isBitPerfect) BitPerfectGreen else AudiophileGold
            )
        }
    }
}

@Composable
fun SignalPathNode(
    icon: ImageVector,
    title: String,
    subtitle: String,
    details: List<Pair<String, String>>,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            details.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 12.sp, color = TextTertiary)
                    Text(
                        text = value,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SignalPathConnector() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(18.dp)
                .background(SurfaceBorder)
        )
    }
}
