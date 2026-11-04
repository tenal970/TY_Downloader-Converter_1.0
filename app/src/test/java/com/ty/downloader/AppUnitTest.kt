package com.ty.downloader

import com.ty.downloader.data.DestinationFolder
import com.ty.downloader.data.DownloadMode
import com.ty.downloader.data.LogManager
import com.ty.downloader.data.PreviewInfo
import com.ty.downloader.data.QualityPresets
import com.ty.downloader.data.SelectedFile
import com.ty.downloader.data.SupportedFormats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUnitTest {

    @Test
    fun testRedactUrl() {
        val urlWithCredentials = "https://admin:supersecret@example.com/video.mp4?token=123"
        val redacted = LogManager.redactUrl(urlWithCredentials)
        assertFalse(redacted.contains("supersecret"))
        assertTrue(redacted.contains("<redacted>@"))
    }

    @Test
    fun testQualityPresets() {
        val videoQualities = QualityPresets.VIDEO_QUALITIES
        assertTrue(videoQualities.isNotEmpty())
        assertTrue(videoQualities.any { it.id == "1080" })
        assertTrue(videoQualities.any { it.id == "best" })

        val audioQualities = QualityPresets.AUDIO_QUALITIES
        assertTrue(audioQualities.isNotEmpty())
        assertTrue(audioQualities.any { it.id == "320" })
    }

    @Test
    fun testSupportedFormats() {
        assertTrue(SupportedFormats.IMAGE_INPUTS.contains("jpg"))
        assertTrue(SupportedFormats.IMAGE_INPUTS.contains("png"))
        assertTrue(SupportedFormats.AUDIO_INPUTS.contains("mp3"))
        assertTrue(SupportedFormats.VIDEO_INPUTS.contains("mp4"))
    }

    @Test
    fun testFormattedDuration() {
        val infoShort = PreviewInfo(durationSeconds = 125L)
        assertEquals("02:05", infoShort.formattedDuration())

        val infoLong = PreviewInfo(durationSeconds = 3665L)
        assertEquals("1:01:05", infoLong.formattedDuration())
    }

    @Test
    fun testSelectedFileProperties() {
        val file = SelectedFile(
            uriString = "content://media/1",
            name = "test_video.mp4",
            sizeBytes = 1048576L * 5,
            kind = com.ty.downloader.data.MediaKind.VIDEO
        )
        assertEquals("test_video.mp4", file.name)
        assertEquals("test_video.mp4", file.displayName)
        assertEquals("5.00 MiB", file.formattedSize())
    }
}
