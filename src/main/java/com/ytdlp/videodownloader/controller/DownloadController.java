package com.ytdlp.videodownloader.controller;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ytdlp.videodownloader.service.DownloadService;


@Controller
public class DownloadController {

    private final DownloadService downloadService;

    // Class Constructor
    public DownloadController(DownloadService downloadService) {

        this.downloadService = downloadService;

    }

    /**
     * Download a YouTube video given a URL and a format.
     *
     * The supported formats are 'mp3' for audio and 'mp4' for video.
     *
     * @param videoUrl the URL of the YouTube video to download
     * @param format the format of the video to download
     * @return a ResponseEntity containing the downloaded resource with appropriate headers for file download
     */
    @PostMapping("/download")
    public ResponseEntity<Resource> download(
            @RequestParam("url") String videoUrl,
            @RequestParam("format") String format) {

        String normalizedURL;

        try {
            // Validate if the provided URL is a valid YouTube URL
            try {

                normalizedURL = isValidYouTubeUrl(videoUrl);

            } catch (Exception e) {
                // Return a bad request response if the URL is not valid
                return ResponseEntity.badRequest()
                    .header("X-Error", "Invalid format: the url is not a valid youtube url.")
                    .body(null);
            }
            
            // Validate if the requested format is supported
            if (!isValidFormat(format)) {
                // Return a bad request response if the format is not valid
                return ResponseEntity.badRequest()
                    .header("X-Error", "Invalid format: only mp3 and mp4 are supported.")
                    .body(null);
            }
            
            // Attempt to download the video using the provided URL and format

            DownloadService.DownloadResult result;

            try {

                result = downloadService.downloadVideo(normalizedURL, format);
            
            } catch (IOException e) {

                return ResponseEntity.badRequest()
                    .header("X-Error", "Error while downloading the media: " + e.getMessage())
                    .body(null);

            }
            
            // Return the downloaded resource with appropriate headers for file download
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + result.filename() + "\"")
                .body(result.resource());
            
        } catch (Exception e) {

            // Handle exceptions by returning an internal server error response
            return ResponseEntity.internalServerError()
                .header("X-Error", "Error while processing data: " + e.getMessage())
                .body(null);
        }
    }

    /**
     * Validates if the provided URL is a valid YouTube URL.
     *
     * @param url the URL to be validated.
     * @return true if the URL is a valid YouTube URL, otherwise false.
     */
    private String isValidYouTubeUrl(String url) {

        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }

        // The regex pattern to match a valid YouTube URL.
        // The pattern is taken from the official YouTube API documentation.
        // https://developers.google.com/youtube/v3/getting-started#terms
        String regex = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)([\\w-]{11})([&?].*)?$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid YouTube URL");
        }

        // The video Id must be 11 characters long.
        String videoId = matcher.group(4);
        if (videoId == null || videoId.length() != 11) {
            throw new IllegalArgumentException("Invalid YouTube URL");
        }

        // Normalize the URL to the watch page URL.
        String normalizedURL = "https://www.youtube.com/watch?v=" + videoId;

        return normalizedURL;
    }

    /**
     * Validates if the provided format is supported.
     *
     * @param format the format to be validated, expects "mp3" or "mp4".
     * @return true if the format is "mp3" or "mp4", otherwise false.
     */
    private boolean isValidFormat(String format) {

        return "mp3".equals(format) || "mp4".equals(format);

    }
}