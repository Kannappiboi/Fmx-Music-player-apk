package com.example.model

enum class AudioCategory {
    DSD,
    LOSSLESS,
    SURROUND,
    STANDARD
}

enum class AudioFormatType(
    val extension: String,
    val displayName: String,
    val category: AudioCategory,
    val isDsd: Boolean = false,
    val isLossless: Boolean = true,
    val isSurround: Boolean = false
) {
    DSD_DSF("dsf", "DSF", AudioCategory.DSD, isDsd = true, isLossless = true),
    DSD_DFF("dff", "DFF", AudioCategory.DSD, isDsd = true, isLossless = true),
    FLAC("flac", "FLAC", AudioCategory.LOSSLESS, isLossless = true),
    ALAC("m4a", "ALAC", AudioCategory.LOSSLESS, isLossless = true),
    WAV("wav", "WAV", AudioCategory.LOSSLESS, isLossless = true),
    PCM("pcm", "PCM", AudioCategory.LOSSLESS, isLossless = true),
    AAC("aac", "AAC", AudioCategory.STANDARD, isLossless = false),
    MP3("mp3", "MP3", AudioCategory.STANDARD, isLossless = false),
    AC3("ac3", "AC-3", AudioCategory.SURROUND, isLossless = false, isSurround = true),
    EAC3("eac3", "E-AC3", AudioCategory.SURROUND, isLossless = false, isSurround = true),
    DTS("dts", "DTS", AudioCategory.SURROUND, isLossless = false, isSurround = true),
    DTS_HD("dtshd", "DTS-HD MA", AudioCategory.SURROUND, isLossless = true, isSurround = true),
    MLP("mlp", "MLP", AudioCategory.SURROUND, isLossless = true, isSurround = true)
}

data class AudioTrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val formatType: AudioFormatType,
    val sampleRate: Int, // e.g. 2822400 for DSD64, 192000, 96000, 44100
    val bitDepth: Int,   // 1 for DSD, 16, 24, 32
    val channels: Int,   // 2 for stereo, 6 for 5.1
    val bitrateKbps: Int,
    val albumArtResId: Int? = null,
    val contentUriString: String? = null,
    val filePath: String? = null,
    val fileSizeBytes: Long = 0L,
    val dynamicRangeScore: Int = 14,
    val isSyntheticMaster: Boolean = false
) {
    val sampleRateLabel: String
        get() = when {
            formatType.isDsd && sampleRate >= 5644800 -> "DSD128 • 5.6 MHz"
            formatType.isDsd -> "DSD64 • 2.8 MHz"
            sampleRate >= 1000 -> "${sampleRate / 1000} kHz"
            else -> "$sampleRate Hz"
        }

    val bitDepthLabel: String
        get() = if (formatType.isDsd) "1-bit PDM" else "$bitDepth-bit"

    val channelConfigLabel: String
        get() = when (channels) {
            1 -> "Mono"
            2 -> "Stereo 2.0"
            6 -> "Surround 5.1"
            8 -> "Surround 7.1"
            else -> "$channels Ch"
        }

    val badgeLabel: String
        get() = when {
            formatType.isDsd -> "DSD64 • 2.8MHz • 1-BIT"
            bitDepth >= 24 && sampleRate >= 96000 -> "${formatType.displayName} • ${sampleRate / 1000}kHz / ${bitDepth}B"
            formatType.isSurround -> "${formatType.displayName} • ${channelConfigLabel}"
            else -> "${formatType.displayName} • ${sampleRateLabel}"
        }
}

enum class DsdDecodeMode(val label: String, val description: String) {
    DIRECT_DSD_TO_PCM(
        "Direct DSD-to-PCM Decimation",
        "Multi-stage FIR 50kHz lowpass filter converting 1-bit PDM to pristine 24-bit/88.2kHz float PCM."
    ),
    DOP_V1_1(
        "DSD over PCM (DoP v1.1 Standard)",
        "Packs 16-bit DSD chunks with 0x05/0xFA markers into 24-bit/176.4kHz frames for external USB DACs."
    )
}

enum class ResamplerMode(val label: String, val description: String) {
    BIT_PERFECT_DIRECT(
        "Bit-Perfect Direct Bypass",
        "Direct Android Audio HAL stream bypassing all OS mixers and rate converters."
    ),
    POLYPHASE_SINC(
        "Polyphase Sinc Ultra",
        "High-order band-limited polyphase interpolation with >140dB stopband attenuation."
    ),
    MINIMUM_PHASE(
        "Minimum Phase Fast Roll-Off",
        "Eliminates pre-ringing artifacts for natural transient response."
    )
}

data class AudioPipelineStatus(
    val inputFormat: String = "FLAC",
    val inputSampleRate: Int = 192000,
    val inputBitDepth: Int = 24,
    val inputChannels: Int = 2,
    val activeDecoder: String = "Native High-Res AudioTrack Decoder",
    val dspProcessing: String = "Bit-Perfect Direct (Bypass)",
    val outputDeviceName: String = "Internal Hi-Res AudioTrack",
    val outputSampleRate: Int = 192000,
    val outputBitDepth: Int = 24,
    val outputChannels: Int = 2,
    val isBitPerfect: Boolean = true,
    val bufferSizeFrames: Int = 4096,
    val latencyMs: Double = 21.3,
    val underrunCount: Int = 0,
    val usbDirectMode: Boolean = true,
    val isUsbDacConnected: Boolean = false
)
