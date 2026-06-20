package com.ytdlp.videodownloader.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DownloadControllerTest {

    @Test
    void shouldNormalizeValidYouTubeUrl() {
        String normalized = DownloadController.isValidYouTubeUrl("https://youtu.be/abcdefghijk");
        assertEquals("https://www.youtube.com/watch?v=abcdefghijk", normalized);
    }

    @SuppressWarnings("")
    @Test
    void shouldRejectInvalidYouTubeUrl() {
        assertThrows(IllegalArgumentException.class, () -> DownloadController.isValidYouTubeUrl("https://example.com"));
    }

    @Test
    void shouldAcceptSupportedFormats() {
        assertTrue(DownloadController.isValidFormat("mp3"));
        assertTrue(DownloadController.isValidFormat("mp4"));
    }

    @Test
    void shouldRejectUnsupportedFormats() {
        assertFalse(DownloadController.isValidFormat("avi"));
    }
}
