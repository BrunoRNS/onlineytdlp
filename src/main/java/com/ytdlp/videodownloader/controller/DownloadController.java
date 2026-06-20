package com.ytdlp.videodownloader.controller;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * Constructor for the DownloadController class.
     * 
     * @param downloadService
     */
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
            try {

                normalizedURL = isValidYouTubeUrl(videoUrl);

            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .header("X-Error", "Invalid format: the url is not a valid youtube url.")
                    .body(null);
            }
            
            if (!isValidFormat(format)) {
                return ResponseEntity.badRequest()
                    .header("X-Error", "Invalid format: only mp3 and mp4 are supported.")
                    .body(null);
            }

            DownloadService.DownloadResult result;

            try {

                result = downloadService.downloadVideo(normalizedURL, format);
            
            } catch (IOException e) {

                return ResponseEntity.badRequest()
                    .header("X-Error", "Error while downloading the media: " + e.getMessage().replaceAll("[\\r\\n]", " "))
                    .body(null);

            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + result.filename() + "\"")
                .body(result.resource());
            
        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                .header("X-Error", "Invalid input: " + e.getMessage())
                .body(null);

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                .header("X-Error", "Error while processing data: " + e.getMessage().replaceAll(
                    "[\\r\\n]", " "
                ))
                .body(null);
        }
    }

    /**
     * Validates if the provided URL is a valid YouTube URL.
     *
     * @param url the URL to be validated.
     * @return true if the URL is a valid YouTube URL, otherwise false.
     */
    public static String isValidYouTubeUrl(String url) 
    throws IllegalArgumentException {

        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }

        // The regex pattern to match a valid YouTube URL.
        // The pattern is taken from the official YouTube API documentation.
        // https://developers.google.com/youtube/v3/getting-started#terms
        String regex = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)([\\w-]{11})([&?].*)?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid YouTube URL");
        }

        String videoId = matcher.group(4);
        if (videoId == null || videoId.length() != 11) {
            throw new IllegalArgumentException("Invalid YouTube URL");
        }

        String normalizedURL = "https://www.youtube.com/watch?v=" + videoId;

        return normalizedURL;
    }

    /**
     * Validates if the provided format is supported.
     *
     * @param format the format to be validated, expects "mp3" or "mp4".
     * @return true if the format is "mp3" or "mp4", otherwise false.
     */
    public static boolean isValidFormat(String format) {

        return "mp3".equals(format) || "mp4".equals(format);

    }
    
}
