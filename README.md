# Resonance — High-Resolution Android Audio Player (.apk)

**Repository:** `Kannappiboi/HIRES-music-player-apk`  
**Platform:** Android (API 26+)  
**Framework:** Jetpack Compose, Kotlin DSL, Android Gradle Plugin (AGP)  
**Architecture:** Clean Architecture + MVVM + Native AudioTrack Pipeline  

---

## Overview

**Resonance** is an audiophile-grade, minimalist Android music player engineered for Bit-Perfect high-resolution audio reproduction. It features a direct native `AudioTrack` pipeline capable of bypassing standard Android OS resampling, native DSD parsing with DoP and decimation filtering, and playback support for lossless, uncompressed, spatial surround, and standard audio formats up to 24-bit/192kHz and 32-bit float.

---

## Key Features & Technical Specifications

### 1. UI / UX Design
- **Material Design 3 (M3)**: Distraction-free, dark-mode-first aesthetic with deep obsidian surfaces and warm champagne gold accents.
- **Gesture-Based Navigation**:
  - **Tap Album Art**: Toggle Play / Pause with haptic-styled spring animations.
  - **Horizontal Drag**: Live audio scrub seeking with floating `+0:15` / `-0:30` timestamp delta indicator.
  - **Horizontal Fling**: Skip to Next / Previous track.
  - **Vertical Swipe Up**: Instant reveal of the real-time Audio Signal Path & HAL telemetry sheet.
- **32-Band FFT Spectrum Analyzer**: Real-time logarithmic frequency visualization with smooth decay response.

### 2. Audio Engine & Decoder Support
- **Audiophile DSD Engine**:
  - Full Sony DSF and Philips DFF container parsing.
  - **Direct DSD-to-PCM Decimation**: 5th-order FIR lowpass filter converting 1-bit PDM stream (2.8224 MHz / 5.6448 MHz) to 24-bit/88.2kHz float PCM for universal DAC compatibility.
  - **DSD-over-PCM (DoP v1.1)**: Packs 16-bit DSD frames with alternating `0x05` and `0xFA` marker bytes at 176.4kHz for native bitstream passthrough to external USB DACs.
- **Lossless & Uncompressed Formats**:
  - **FLAC**: Studio Master up to 24-bit / 192 kHz.
  - **ALAC**: Apple Lossless up to 24-bit / 96 kHz.
  - **WAV & LPCM**: Up to 32-bit floating point and 24-bit packed PCM.
- **Surround Sound Demuxing**:
  - **AC-3 & E-AC3 (Dolby Digital Plus / Atmos)**: Multi-channel 5.1/7.1 demuxing.
  - **DTS & DTS-HD Master Audio**: Multi-channel spatial audio streams.
  - **MLP (Meridian Lossless Packing)**: DVD-Audio lossless surround stream parsing.
- **Compressed & Standard**:
  - **MP3**: Up to 320 kbps CBR/VBR.
  - **AAC**: High-Efficiency and Advanced Audio Coding.

### 3. Bit-Perfect Hardware HAL & USB Audio Direct
- **Low-Latency Direct AudioTrack**: Configured with `AudioAttributes.FLAG_HW_AV_SYNC` and `AudioFormat.ENCODING_PCM_FLOAT` to bypass the Android system mixer when supported.
- **USB Audio Class 2 (UAC2) Direct Output**: Automatic detection of connected USB DACs, reporting real-time endpoint parameters, buffer frames, hardware latency (ms), and underrun counters.
- **Bit-Perfect Direct Indicator**: Live emerald status pill confirming non-resampled direct hardware transmission.

### 4. Audiophile DSP & Tone Control
- **Pure Direct Bypass**: Single-tap bypass of all software processing for unadulterated source bitstream output.
- **10-Band Parametric Equalizer**: 31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz (-12dB to +12dB).
- **Bauer Headphone Crossfeed**: Spatial acoustic simulation cross-feeding delayed low-frequency content between channels to prevent listening fatigue on headphones.

---

## Project Structure

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt                      # Edge-to-edge entry point & intent handler
│   │   │   ├── engine/
│   │   │   │   ├── AudioHalInspector.kt             # Hardware HAL, USB DAC & buffer telemetry
│   │   │   │   ├── DsdEngine.kt                     # DSF/DFF parser, DoP v1.1, FIR decimation
│   │   │   │   └── HighResAudioEngine.kt            # Direct AudioTrack streaming & DSP pipeline
│   │   │   ├── model/
│   │   │   │   └── AudioModels.kt                   # Track metadata, formats & pipeline state
│   │   │   ├── repository/
│   │   │   │   └── AudioTrackRepository.kt          # Showcase masters & SAF file importer
│   │   │   └── ui/
│   │   │       ├── AudioPlayerViewModel.kt          # MVVM state management & playback controls
│   │   │       ├── ResonancePlayerScreen.kt         # Primary player UI & gesture coordinators
│   │   │       ├── components/
│   │   │       │   ├── AudioSettingsSheet.kt        # DSD & resampler configuration modal
│   │   │       │   ├── EqualizerSheet.kt            # 10-band EQ & Bauer crossfeed modal
│   │   │       │   ├── GestureAlbumArt.kt           # Gesture-driven hero vinyl artwork
│   │   │       │   ├── SignalPathSheet.kt           # Bit-perfect signal flow inspector
│   │   │       │   ├── TrackQueueSheet.kt           # Library browser & SAF file import
│   │   │       │   └── Visualizer.kt                # Real-time FFT spectrum visualizer
│   │   │       └── theme/
│   │   │           ├── Color.kt                     # Audiophile obsidian & gold color palette
│   │   │           ├── Theme.kt                     # Material 3 dark-mode-first theme
│   │   │           └── Type.kt                      # M3 typography definitions
│   │   ├── res/
│   │   │   ├── drawable/                            # Generated adaptive icons & album art
│   │   │   ├── mipmap-*/                            # Multi-density adaptive & round launcher icons
│   │   │   └── values/                              # strings.xml, colors.xml, themes.xml
│   │   └── AndroidManifest.xml                      # Foreground service & audio permissions
│   └── test/
│       └── java/com/example/
│           ├── ExampleRobolectricTest.kt            # Robolectric JVM tests for DSD & HAL
│           └── ExampleUnitTest.kt                   # Unit tests
├── build.gradle.kts
└── proguard-rules.pro
```

---

## Build & Run Instructions

### 1. Compile Debug APK
```bash
gradle assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Run Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

---

## Pushing to GitHub (`Kannappiboi/HIRES-music-player-apk`)

To push this codebase to your GitHub repository:
1. Initialize git and add files:
   ```bash
   git init
   git add .
   git commit -m "feat: initial release of Resonance Hi-Res Audio Player"
   ```
2. Link your remote repository and push:
   ```bash
   git remote add origin https://github.com/Kannappiboi/HIRES-music-player-apk.git
   git branch -M main
   git push -u origin main
   ```
Or use the **GitHub** button in the AI Studio settings menu to directly link and push your repository.
