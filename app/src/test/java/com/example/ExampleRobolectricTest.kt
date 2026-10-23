package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.DsdEngine
import com.example.model.AudioFormatType
import com.example.repository.AudioTrackRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Resonance", appName)
  }

  @Test
  fun `verify audiophile repository has showcase tracks`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = AudioTrackRepository(context)
    val tracks = repository.tracks.value
    assertTrue("Showcase tracks should not be empty", tracks.isNotEmpty())

    val dsdTrack = tracks.find { it.formatType.isDsd }
    assertNotNull("DSD track should be present", dsdTrack)
    assertEquals(2822400, dsdTrack?.sampleRate)

    val flacTrack = tracks.find { it.formatType == AudioFormatType.FLAC }
    assertNotNull("FLAC track should be present", flacTrack)
    assertEquals(192000, flacTrack?.sampleRate)
    assertEquals(24, flacTrack?.bitDepth)
  }

  @Test
  fun `verify DSD DoP marker packing`() {
    // 32 bytes of DSD dummy bitstream
    val dummyDsd = ByteArray(32) { 0x55.toByte() }
    val dopPcm = DsdEngine.packDsdToDoP(dummyDsd, channels = 2)
    // 32 bytes / 2 channels = 16 bytes per ch -> 8 frames -> 8 frames * 2 ch * 3 bytes = 48 bytes
    assertEquals(48, dopPcm.size)
  }
}
