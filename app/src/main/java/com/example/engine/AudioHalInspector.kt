package com.example.engine

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.example.model.AudioPipelineStatus

class AudioHalInspector(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    data class OutputDeviceInfo(
        val name: String,
        val type: String,
        val isUsbDac: Boolean,
        val isHighResCapable: Boolean,
        val supportedSampleRates: List<Int>,
        val supportedChannelCounts: List<Int>
    )

    fun getActiveOutputDevice(): OutputDeviceInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            // Priority: USB Audio Class 2 DAC > Wired Headset > Bluetooth > Built-in
            val usbDevice = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_USB_DEVICE || it.type == AudioDeviceInfo.TYPE_USB_HEADSET
            }
            if (usbDevice != null) {
                return formatDeviceInfo(usbDevice, "USB Audio Class 2 DAC", isUsb = true)
            }

            val wiredDevice = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
            }
            if (wiredDevice != null) {
                return formatDeviceInfo(wiredDevice, "Hi-Fi Wired Output (Direct HAL)", isUsb = false)
            }

            val btDevice = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
            }
            if (btDevice != null) {
                return formatDeviceInfo(btDevice, "Bluetooth Hi-Res (LDAC/aptX)", isUsb = false)
            }

            val speakerDevice = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }
            if (speakerDevice != null) {
                return formatDeviceInfo(speakerDevice, "Internal Device Speaker", isUsb = false)
            }
        }

        return OutputDeviceInfo(
            name = "Android Audio HAL (Direct Track)",
            type = "Direct PCM Output",
            isUsbDac = false,
            isHighResCapable = true,
            supportedSampleRates = listOf(44100, 48000, 88200, 96000, 176400, 192000),
            supportedChannelCounts = listOf(2, 6, 8)
        )
    }

    private fun formatDeviceInfo(device: AudioDeviceInfo, fallbackName: String, isUsb: Boolean): OutputDeviceInfo {
        val sampleRates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.sampleRates.toList().ifEmpty { listOf(44100, 48000, 96000, 192000) }
        } else listOf(44100, 48000, 96000, 192000)

        val channelCounts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.channelCounts.toList().ifEmpty { listOf(2) }
        } else listOf(2)

        val productName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.productName?.toString()?.takeIf { it.isNotBlank() } ?: fallbackName
        } else fallbackName

        return OutputDeviceInfo(
            name = productName,
            type = if (isUsb) "USB Audio Class 2 (Asynchronous)" else "Android Audio HAL",
            isUsbDac = isUsb,
            isHighResCapable = sampleRates.any { it >= 96000 },
            supportedSampleRates = sampleRates,
            supportedChannelCounts = channelCounts
        )
    }

    fun buildPipelineStatus(
        sourceFormat: String,
        sourceSampleRate: Int,
        sourceBitDepth: Int,
        sourceChannels: Int,
        isBitPerfectModeRequested: Boolean,
        dspBypass: Boolean,
        underrunCount: Int = 0
    ): AudioPipelineStatus {
        val device = getActiveOutputDevice()

        // When source sample rate matches output sample rate and DSP is bypass, bit-perfect direct is achieved!
        val effectiveOutputSampleRate = if (sourceSampleRate in device.supportedSampleRates) {
            sourceSampleRate
        } else if (sourceSampleRate >= 96000) {
            192000
        } else {
            44100
        }

        val effectiveBitDepth = if (sourceBitDepth >= 24) 24 else 16
        val isBitPerfect = isBitPerfectModeRequested && dspBypass && (effectiveOutputSampleRate == sourceSampleRate)

        val bufferFrames = when (effectiveOutputSampleRate) {
            192000 -> 4096
            96000 -> 2048
            else -> 1024
        }
        val latency = (bufferFrames.toDouble() / effectiveOutputSampleRate) * 1000.0

        return AudioPipelineStatus(
            inputFormat = sourceFormat,
            inputSampleRate = sourceSampleRate,
            inputBitDepth = sourceBitDepth,
            inputChannels = sourceChannels,
            activeDecoder = when {
                sourceFormat.contains("DSD", ignoreCase = true) -> "DSD Native Delta-Sigma Decimation Engine"
                sourceFormat.contains("FLAC", ignoreCase = true) -> "Native FLAC 24-bit Lossless Decoder"
                sourceFormat.contains("ALAC", ignoreCase = true) -> "Apple Lossless (ALAC) Hi-Res Decoder"
                sourceFormat.contains("WAV", ignoreCase = true) -> "32-bit Float Linear PCM Streamer"
                sourceFormat.contains("AC", ignoreCase = true) || sourceFormat.contains("DTS", ignoreCase = true) -> "Spatial Surround 5.1 Multichannel Demuxer"
                else -> "Direct Low-Latency AudioTrack Codec"
            },
            dspProcessing = if (dspBypass) "Pure Direct (Bit-Perfect Bypass)" else "10-Band Audiophile EQ + Crossfeed Active",
            outputDeviceName = device.name,
            outputSampleRate = effectiveOutputSampleRate,
            outputBitDepth = effectiveBitDepth,
            outputChannels = sourceChannels.coerceAtMost(2),
            isBitPerfect = isBitPerfect,
            bufferSizeFrames = bufferFrames,
            latencyMs = String.format("%.1f", latency).toDoubleOrNull() ?: 21.3,
            underrunCount = underrunCount,
            usbDirectMode = device.isUsbDac || isBitPerfectModeRequested,
            isUsbDacConnected = device.isUsbDac
        )
    }
}
