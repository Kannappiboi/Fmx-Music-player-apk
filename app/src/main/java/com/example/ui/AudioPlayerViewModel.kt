package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AudioHalInspector
import com.example.engine.HighResAudioEngine
import com.example.model.AudioCategory
import com.example.model.AudioFormatType
import com.example.model.AudioPipelineStatus
import com.example.model.AudioTrackItem
import com.example.model.DsdDecodeMode
import com.example.model.ResamplerMode
import com.example.repository.AudioTrackRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class PlayerSheet {
    NONE, QUEUE, SIGNAL_PATH, EQUALIZER, SETTINGS
}

class AudioPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudioTrackRepository(application)
    private val halInspector = AudioHalInspector(application)
    val audioEngine = HighResAudioEngine(application)

    val tracks: StateFlow<List<AudioTrackItem>> = repository.tracks

    private val _currentTrack = MutableStateFlow<AudioTrackItem?>(null)
    val currentTrack: StateFlow<AudioTrackItem?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _spectrumLevels = MutableStateFlow(FloatArray(32) { 0.05f })
    val spectrumLevels: StateFlow<FloatArray> = _spectrumLevels.asStateFlow()

    private val _isPureDirect = MutableStateFlow(true)
    val isPureDirect: StateFlow<Boolean> = _isPureDirect.asStateFlow()

    private val _isCrossfeedEnabled = MutableStateFlow(false)
    val isCrossfeedEnabled: StateFlow<Boolean> = _isCrossfeedEnabled.asStateFlow()

    private val _dsdDecodeMode = MutableStateFlow(DsdDecodeMode.DIRECT_DSD_TO_PCM)
    val dsdDecodeMode: StateFlow<DsdDecodeMode> = _dsdDecodeMode.asStateFlow()

    private val _resamplerMode = MutableStateFlow(ResamplerMode.BIT_PERFECT_DIRECT)
    val resamplerMode: StateFlow<ResamplerMode> = _resamplerMode.asStateFlow()

    // 10-band EQ frequencies: 31, 62, 125, 250, 500, 1k, 2k, 4k, 8k, 16k
    private val _eqBands = MutableStateFlow(List(10) { 0.0f })
    val eqBands: StateFlow<List<Float>> = _eqBands.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _activeSheet = MutableStateFlow(PlayerSheet.NONE)
    val activeSheet: StateFlow<PlayerSheet> = _activeSheet.asStateFlow()

    private val _selectedCategory = MutableStateFlow<AudioCategory?>(null)
    val selectedCategory: StateFlow<AudioCategory?> = _selectedCategory.asStateFlow()

    private val _pipelineStatus = MutableStateFlow(
        halInspector.buildPipelineStatus(
            sourceFormat = "FLAC",
            sourceSampleRate = 192000,
            sourceBitDepth = 24,
            sourceChannels = 2,
            isBitPerfectModeRequested = true,
            dspBypass = true
        )
    )
    val pipelineStatus: StateFlow<AudioPipelineStatus> = _pipelineStatus.asStateFlow()

    init {
        // Default to first track
        viewModelScope.launch {
            repository.tracks.collect { list ->
                if (_currentTrack.value == null && list.isNotEmpty()) {
                    _currentTrack.value = list.first()
                    updatePipelineStatus(list.first())
                }
            }
        }

        // Engine callbacks
        audioEngine.onPositionChanged = { pos ->
            _playbackPositionMs.value = pos
        }

        audioEngine.onTrackCompleted = {
            handleTrackCompletion()
        }

        // Real-time spectrum polling loop
        viewModelScope.launch {
            while (isActive) {
                if (_isPlaying.value) {
                    _spectrumLevels.value = audioEngine.spectrumLevels
                } else {
                    // Decay spectrum gracefully
                    val current = _spectrumLevels.value
                    val decayed = FloatArray(current.size) { i ->
                        (current[i] * 0.85f).coerceAtLeast(0.04f)
                    }
                    _spectrumLevels.value = decayed
                }
                delay(33) // ~30 FPS spectrum updates
            }
        }
    }

    private fun updatePipelineStatus(track: AudioTrackItem) {
        val sampleRate = if (track.formatType.isDsd) {
            when (_dsdDecodeMode.value) {
                DsdDecodeMode.DIRECT_DSD_TO_PCM -> 88200
                DsdDecodeMode.DOP_V1_1 -> 176400
            }
        } else {
            track.sampleRate
        }

        _pipelineStatus.value = halInspector.buildPipelineStatus(
            sourceFormat = track.formatType.displayName,
            sourceSampleRate = sampleRate,
            sourceBitDepth = track.bitDepth,
            sourceChannels = track.channels,
            isBitPerfectModeRequested = _resamplerMode.value == ResamplerMode.BIT_PERFECT_DIRECT,
            dspBypass = _isPureDirect.value,
            underrunCount = audioEngine.underrunCount
        )
    }

    fun selectTrack(track: AudioTrackItem) {
        _currentTrack.value = track
        updatePipelineStatus(track)
        audioEngine.playTrack(track, 0L)
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        val track = _currentTrack.value ?: return
        if (_isPlaying.value) {
            audioEngine.pause()
            _isPlaying.value = false
        } else {
            if (audioEngine.currentTrack?.id == track.id) {
                audioEngine.resume()
            } else {
                audioEngine.playTrack(track, _playbackPositionMs.value)
            }
            _isPlaying.value = true
        }
    }

    fun seekTo(positionMs: Long) {
        _playbackPositionMs.value = positionMs
        audioEngine.seekTo(positionMs)
    }

    fun nextTrack() {
        val currentList = tracks.value
        if (currentList.isEmpty()) return
        val currentIndex = currentList.indexOfFirst { it.id == _currentTrack.value?.id }
        val nextIndex = if (_isShuffle.value) {
            (currentList.indices).random()
        } else {
            (currentIndex + 1) % currentList.size
        }
        selectTrack(currentList[nextIndex])
    }

    fun prevTrack() {
        val currentList = tracks.value
        if (currentList.isEmpty()) return
        if (_playbackPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }
        val currentIndex = currentList.indexOfFirst { it.id == _currentTrack.value?.id }
        val prevIndex = if (currentIndex <= 0) currentList.lastIndex else currentIndex - 1
        selectTrack(currentList[prevIndex])
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                _currentTrack.value?.let { selectTrack(it) }
            }
            RepeatMode.ALL -> {
                nextTrack()
            }
            RepeatMode.OFF -> {
                _isPlaying.value = false
                _playbackPositionMs.value = 0L
            }
        }
    }

    fun togglePureDirect() {
        val newState = !_isPureDirect.value
        _isPureDirect.value = newState
        audioEngine.isPureDirectBypass = newState
        _currentTrack.value?.let { updatePipelineStatus(it) }
    }

    fun toggleCrossfeed() {
        val newState = !_isCrossfeedEnabled.value
        _isCrossfeedEnabled.value = newState
        audioEngine.isCrossfeedEnabled = newState
        // If enabling crossfeed, pure direct is turned off to allow DSP
        if (newState && _isPureDirect.value) {
            _isPureDirect.value = false
            audioEngine.isPureDirectBypass = false
        }
        _currentTrack.value?.let { updatePipelineStatus(it) }
    }

    fun setDsdDecodeMode(mode: DsdDecodeMode) {
        _dsdDecodeMode.value = mode
        audioEngine.dsdDecodeMode = mode
        _currentTrack.value?.let { updatePipelineStatus(it) }
    }

    fun setResamplerMode(mode: ResamplerMode) {
        _resamplerMode.value = mode
        audioEngine.resamplerMode = mode
        _currentTrack.value?.let { updatePipelineStatus(it) }
    }

    fun setEqBand(index: Int, gainDb: Float) {
        val list = _eqBands.value.toMutableList()
        if (index in list.indices) {
            list[index] = gainDb.coerceIn(-12.0f, 12.0f)
            _eqBands.value = list
            audioEngine.eqGains[index] = gainDb
            // If tweaking EQ, pure direct bypass is deactivated
            if (_isPureDirect.value) {
                _isPureDirect.value = false
                audioEngine.isPureDirectBypass = false
            }
            _currentTrack.value?.let { updatePipelineStatus(it) }
        }
    }

    fun resetEq() {
        _eqBands.value = List(10) { 0.0f }
        for (i in 0 until 10) audioEngine.eqGains[i] = 0.0f
        _currentTrack.value?.let { updatePipelineStatus(it) }
    }

    fun applyEqPreset(preset: String) {
        val gains = when (preset) {
            "Acoustic" -> listOf(2f, 3f, 1f, 0f, 1f, 2f, 3f, 2f, 1f, 0f)
            "Bass Boost" -> listOf(6f, 5f, 4f, 2f, 0f, 0f, 0f, 0f, 0f, 0f)
            "Treble Air" -> listOf(0f, 0f, 0f, 0f, 0f, 1f, 3f, 4f, 5f, 6f)
            "Vocal Clarity" -> listOf(-1f, 0f, 1f, 3f, 4f, 3f, 2f, 1f, 0f, -1f)
            else -> List(10) { 0.0f } // Flat
        }
        _eqBands.value = gains
        for (i in gains.indices) audioEngine.eqGains[i] = gains[i]
        _isPureDirect.value = false
        audioEngine.isPureDirectBypass = false
        _currentTrack.value?.let { updatePipelineStatus(it) }
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun openSheet(sheet: PlayerSheet) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = PlayerSheet.NONE
    }

    fun setFilterCategory(category: AudioCategory?) {
        _selectedCategory.value = category
    }

    fun importAudioFile(uri: Uri) {
        viewModelScope.launch {
            val imported = repository.importTrackFromUri(uri)
            if (imported != null) {
                selectTrack(imported)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
