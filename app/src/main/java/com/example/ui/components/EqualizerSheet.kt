package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun EqualizerSheet(
    isPureDirect: Boolean,
    isCrossfeedEnabled: Boolean,
    eqBands: List<Float>,
    onTogglePureDirect: () -> Unit,
    onToggleCrossfeed: () -> Unit,
    onBandChanged: (Int, Float) -> Unit,
    onResetEq: () -> Unit,
    onApplyPreset: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    val bandFreqLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    val presets = listOf("Flat", "Acoustic", "Bass Boost", "Treble Air", "Vocal Clarity")

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
                .testTag("equalizer_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUDIOPHILE DSP & EQ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = AudiophileGold
                    )
                    Text(
                        text = "10-Band Parametric Filter Stage",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onResetEq,
                    modifier = Modifier.testTag("reset_eq_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset EQ",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pure Direct Switch card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, if (isPureDirect) BitPerfectGreen else SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pure Direct Bypass",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPureDirect) BitPerfectGreen else TextPrimary
                        )
                        Text(
                            text = "Bypasses all DSP filters for bit-accurate source reproduction",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = isPureDirect,
                        onCheckedChange = { onTogglePureDirect() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BitPerfectGreen,
                            checkedTrackColor = BitPerfectGreen.copy(alpha = 0.2f),
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = SurfaceElevated
                        ),
                        modifier = Modifier.testTag("pure_direct_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Headphone Crossfeed Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Headphone Crossfeed (Bauer)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Reduces acoustic listening fatigue on headphones by cross-mixing binaural bass",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = isCrossfeedEnabled,
                        onCheckedChange = { onToggleCrossfeed() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricCyan,
                            checkedTrackColor = ElectricCyan.copy(alpha = 0.2f),
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = SurfaceElevated
                        ),
                        modifier = Modifier.testTag("crossfeed_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Presets row
            Text(
                text = "PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .clickable { onApplyPreset(preset) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = preset,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 10-band EQ sliders
            Text(
                text = "FREQUENCY BANDS (-12dB to +12dB)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(12.dp))

            eqBands.forEachIndexed { index, gain ->
                val freqLabel = bandFreqLabels.getOrNull(index) ?: "${index + 1}"
                val gainText = if (gain > 0f) "+${gain.toInt()} dB" else "${gain.toInt()} dB"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${freqLabel}Hz",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.width(52.dp)
                    )

                    Slider(
                        value = gain,
                        onValueChange = { onBandChanged(index, it) },
                        valueRange = -12f..12f,
                        enabled = !isPureDirect,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isPureDirect) TextTertiary else AudiophileGold,
                            activeTrackColor = if (isPureDirect) SurfaceBorder else AudiophileGold,
                            inactiveTrackColor = SurfaceElevated
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("eq_slider_$index")
                    )

                    Text(
                        text = gainText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isPureDirect) TextTertiary else TextPrimary,
                        modifier = Modifier
                            .width(54.dp)
                            .padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
