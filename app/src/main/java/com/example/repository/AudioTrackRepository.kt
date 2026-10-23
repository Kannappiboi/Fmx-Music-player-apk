package com.example.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.R
import com.example.model.AudioCategory
import com.example.model.AudioFormatType
import com.example.model.AudioTrackItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AudioTrackRepository(private val context: Context) {

    private val _tracks = MutableStateFlow<List<AudioTrackItem>>(emptyList())
    val tracks: StateFlow<List<AudioTrackItem>> = _tracks.asStateFlow()

    init {
        loadDefaultAudiophileShowcase()
    }

    private fun loadDefaultAudiophileShowcase() {
        val showcaseList = listOf(
            AudioTrackItem(
                id = "dsd_01",
                title = "Acoustic Chamber Concerto - Allegro",
                artist = "Vienna Chamber Soloists",
                album = "Harmonic Resonance (DSD Master Tape)",
                durationMs = 214000L, // 3m 34s
                formatType = AudioFormatType.DSD_DSF,
                sampleRate = 2822400, // DSD64 2.8224 MHz
                bitDepth = 1,
                channels = 2,
                bitrateKbps = 5645,
                albumArtResId = R.drawable.album_art_dsd_1792750047010,
                dynamicRangeScore = 16,
                isSyntheticMaster = true
            ),
            AudioTrackItem(
                id = "flac_02",
                title = "Symphonic Overture No. 9 in D Minor",
                artist = "Royal Philharmonic Orchestra",
                album = "Aura Audiophile Series Vol. 1",
                durationMs = 278000L, // 4m 38s
                formatType = AudioFormatType.FLAC,
                sampleRate = 192000, // 24-bit / 192 kHz
                bitDepth = 24,
                channels = 2,
                bitrateKbps = 5824,
                albumArtResId = R.drawable.album_art_audiophile_1792750033083,
                dynamicRangeScore = 15,
                isSyntheticMaster = true
            ),
            AudioTrackItem(
                id = "alac_03",
                title = "Ambient Horizons - Stellar Drift",
                artist = "Aether Sound Labs",
                album = "Cosmic Frequencies (Lossless)",
                durationMs = 192000L, // 3m 12s
                formatType = AudioFormatType.ALAC,
                sampleRate = 96000, // 24-bit / 96 kHz
                bitDepth = 24,
                channels = 2,
                bitrateKbps = 2950,
                albumArtResId = R.drawable.album_art_audiophile_1792750033083,
                dynamicRangeScore = 14,
                isSyntheticMaster = true
            ),
            AudioTrackItem(
                id = "wav_04",
                title = "Prism Harmonic Study (Direct PCM)",
                artist = "Studio Master Reference",
                album = "32-Bit Floating Point Masters",
                durationMs = 164000L, // 2m 44s
                formatType = AudioFormatType.WAV,
                sampleRate = 96000,
                bitDepth = 32,
                channels = 2,
                bitrateKbps = 6144,
                albumArtResId = R.drawable.album_art_dsd_1792750047010,
                dynamicRangeScore = 17,
                isSyntheticMaster = true
            ),
            AudioTrackItem(
                id = "eac3_05",
                title = "Spatial Aurora Soundscape",
                artist = "Dolby Surround Ensemble",
                album = "Cinematic Multi-Channel 5.1",
                durationMs = 245000L, // 4m 05s
                formatType = AudioFormatType.EAC3,
                sampleRate = 48000,
                bitDepth = 24,
                channels = 6, // 5.1 surround
                bitrateKbps = 640,
                albumArtResId = R.drawable.album_art_audiophile_1792750033083,
                dynamicRangeScore = 13,
                isSyntheticMaster = true
            ),
            AudioTrackItem(
                id = "dtshd_06",
                title = "Celestial Echoes in DTS-HD",
                artist = "Horizon Immersion Studio",
                album = "DTS-HD Master Sound Series",
                durationMs = 220000L, // 3m 40s
                formatType = AudioFormatType.DTS_HD,
                sampleRate = 96000,
                bitDepth = 24,
                channels = 6, // 5.1 surround
                bitrateKbps = 4500,
                albumArtResId = R.drawable.album_art_dsd_1792750047010,
                dynamicRangeScore = 15,
                isSyntheticMaster = true
            ),
            AudioTrackItem(
                id = "mp3_07",
                title = "Midnight In Blue",
                artist = "Crescent Jazz Trio",
                album = "After Hours Sessions",
                durationMs = 186000L, // 3m 06s
                formatType = AudioFormatType.MP3,
                sampleRate = 44100,
                bitDepth = 16,
                channels = 2,
                bitrateKbps = 320,
                albumArtResId = R.drawable.album_art_audiophile_1792750033083,
                dynamicRangeScore = 11,
                isSyntheticMaster = true
            )
        )
        _tracks.value = showcaseList
    }

    /**
     * Imports an audio file selected by the user via Storage Access Framework (SAF).
     */
    suspend fun importTrackFromUri(uri: Uri): AudioTrackItem? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
                ?: "Imported Track"

            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                ?: "Audiophile Library"

            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: "Local Import"

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 180000L

            val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            val bitrateKbps = (bitrateStr?.toIntOrNull() ?: 1411200) / 1000

            // Determine format from filename / mime
            val fileName = uri.lastPathSegment?.lowercase() ?: ""
            val formatType = when {
                fileName.endsWith(".dsf") -> AudioFormatType.DSD_DSF
                fileName.endsWith(".dff") -> AudioFormatType.DSD_DFF
                fileName.endsWith(".flac") -> AudioFormatType.FLAC
                fileName.endsWith(".alac") || fileName.endsWith(".m4a") -> AudioFormatType.ALAC
                fileName.endsWith(".wav") -> AudioFormatType.WAV
                fileName.endsWith(".aac") -> AudioFormatType.AAC
                fileName.endsWith(".ac3") -> AudioFormatType.AC3
                fileName.endsWith(".eac3") -> AudioFormatType.EAC3
                fileName.endsWith(".dts") -> AudioFormatType.DTS
                fileName.endsWith(".dtshd") -> AudioFormatType.DTS_HD
                fileName.endsWith(".mlp") -> AudioFormatType.MLP
                else -> AudioFormatType.MP3
            }

            val sampleRate = when (formatType) {
                AudioFormatType.DSD_DSF, AudioFormatType.DSD_DFF -> 2822400
                AudioFormatType.FLAC, AudioFormatType.ALAC -> 96000
                else -> 44100
            }

            val bitDepth = when (formatType) {
                AudioFormatType.DSD_DSF, AudioFormatType.DSD_DFF -> 1
                AudioFormatType.FLAC, AudioFormatType.ALAC, AudioFormatType.DTS_HD -> 24
                AudioFormatType.WAV -> 24
                else -> 16
            }

            val imported = AudioTrackItem(
                id = "user_${System.currentTimeMillis()}",
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                formatType = formatType,
                sampleRate = sampleRate,
                bitDepth = bitDepth,
                channels = if (formatType.isSurround) 6 else 2,
                bitrateKbps = bitrateKbps,
                albumArtResId = R.drawable.album_art_audiophile_1792750033083,
                contentUriString = uri.toString(),
                dynamicRangeScore = 14,
                isSyntheticMaster = false
            )

            _tracks.value = listOf(imported) + _tracks.value
            imported
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }
}
