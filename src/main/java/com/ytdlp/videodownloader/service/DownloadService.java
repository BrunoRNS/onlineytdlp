package com.ytdlp.videodownloader.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    public record DownloadResult(Resource resource, String filename) {
    }

    protected String ytDlpPath = "/usr/local/bin/ytdlp";

    /**
     * Generates the path to the yt-dlp executable.
     * 
     * The path is generated based on the current operating system and the location of the jar file.
     * It gets the ytdlp path for 64 and 32 bits operating systems.
     * 
     */
    private void generateYtDlpPath() {
        
        String imagePath = System.getProperty("org.graalvm.nativeimage.imagepath");
        Path executablePath;

        if (imagePath != null) {
            executablePath = Paths.get(imagePath).getParent();

        } else {

            try {

                String jarPath = DownloadService.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
                        .getPath();

                Path p = Paths.get(jarPath);
                executablePath = Files.isDirectory(p) ? p : p.getParent();

            } catch (Exception e) {

                executablePath = Paths.get(System.getProperty("user.dir"));

            }

        }

        String ytdlpName = System.getProperty("os.name").toLowerCase().contains("win") ? "ytdlp.exe" : "ytdlp";
        this.ytDlpPath = executablePath.resolve(ytdlpName).toAbsolutePath().toString();

    }

    /**
     * Downloads a YouTube video given a URL and a format.
     * 
     * Downloads the video using yt-dlp and saves it to a temporary file.
     * The file is then read into a byte array and returned as a Resource.
     * The filename is sanitized to remove any special characters.
     * 
     * @param videoUrl the URL of the YouTube video to download
     * @param format   the format of the video to download
     * @return a DownloadResult containing the downloaded resource and its filename
     * @throws IOException if there is an error while downloading the video
     */
    public DownloadResult downloadVideo(String videoUrl, String format) throws IOException {

        String title = UUID.randomUUID().toString();
        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), title + "." + format);

        this.download(videoUrl, tempPath.toString(), format);

        try {
            byte[] fileContent = Files.readAllBytes(tempPath);
            Resource resource = new ByteArrayResource(fileContent);
            String filename = sanitizeFilename(videoUrl, format) + "." + format;

            return new DownloadResult(resource, filename);

        } finally {

            Files.deleteIfExists(tempPath);

        }
    }

    /**
     * Downloads a YouTube video given a URL and a format.
     * 
     * It downloads the video using yt-dlp and saves it to the specified folder.
     * Use with Swing embedder not with Spring Boot directly.
     * 
     * @param videoUrl
     * @param format
     * @param pastaDestino
     * @throws IOException
     */
    public void downloadVideoLocally(String videoUrl, String format, String destFolder) throws IOException {

        String sanitizedName = sanitizeFilename(videoUrl, format);
        Path finalPath = Paths.get(destFolder, sanitizedName + "." + format);

        this.generateYtDlpPath();

        this.download(videoUrl, finalPath.toString(), format);

    }

    /**
     * Downloads a YouTube video given a URL and a format.
     * 
     * It downloads the video using yt-dlp and saves it to the specified file path.
     * 
     * @param videoUrl the URL of the YouTube video to download
     * @param filePath the path where the video will be saved
     * @param format   the format of the video to download
     * @throws IOException if there is an error while downloading the video
     */
    private void download(String videoUrl, String filePath, String format) throws IOException {

        ProcessBuilder processBuilder;

        switch (format) {

            case "mp4" -> processBuilder = new ProcessBuilder(this.ytDlpPath, videoUrl, filePath, format);

            case "mp3" -> processBuilder = new ProcessBuilder(this.ytDlpPath, videoUrl, filePath, format);

            default -> throw new IllegalArgumentException("Invalid format: " + format);

        }

        Process process = processBuilder.start();

        int exitCode = 0;

        try {

            exitCode = process.waitFor();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new IOException("Failed to download video, interrupted: " + e.toString());

        }

        if (exitCode != 0) {

            throw new IOException("Failed to download video: " + new String(process.getErrorStream().readAllBytes()));

        }
    }

    /**
     * Sanitizes a filename by replacing spaces with underscores.
     * 
     * It does this by calling yt-dlp with the --print flag and the title format
     * option.
     * It then takes the first line of the output as the filename.
     * 
     * @param urlVideo the URL of the video.
     * @return the sanitized filename.
     * @throws RuntimeException if there is an error while sanitizing the filename.
     */
    private String sanitizeFilename(String urlVideo, String format) throws RuntimeException {

        ProcessBuilder processBuilder = new ProcessBuilder(this.ytDlpPath, "--print-title", urlVideo);

        Process process;

        try {

            process = processBuilder.start();

        } catch (IOException ex) {

            throw new RuntimeException(ex);

        }

        try {

            process.waitFor();

        } catch (InterruptedException ex) {

            throw new RuntimeException(ex);

        }

        try (final Scanner SCANNER = new Scanner(process.getInputStream())) {

            if (!SCANNER.hasNextLine()) {

                switch (format) {
                    case "mp4" -> {
                        return "video" + UUID.randomUUID().toString();
                    }
                    case "mp3" -> {
                        return "music" + UUID.randomUUID().toString();
                    }
                }

            }

            return SCANNER.nextLine().replace(" ", "_");

        } catch (Exception ex) {

            throw new RuntimeException(ex);

        }

    }

}