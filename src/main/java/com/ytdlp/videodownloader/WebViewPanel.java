package com.ytdlp.videodownloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

import com.ytdlp.videodownloader.service.DownloadService;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class WebViewPanel extends JFXPanel {

    private static final String SPRING_APP_URL = "http://localhost:8080/";
    private static final Path DOWNLOAD_FOLDER = Paths.get(
        System.getProperty("user.home"), "Downloads"
    );

    public WebViewPanel() {
        Platform.runLater(this::initialize);
    }

    private void initialize() {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        engine.getLoadWorker().stateProperty().addListener(
            (observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                injectDownloadInterceptor(engine);
            }
        });

        this.setScene(new Scene(webView));
        engine.load(SPRING_APP_URL);
    }

    private void injectDownloadInterceptor(WebEngine engine) {
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("app", new DesktopDownloadBridge());

        String script = "if (typeof window.downloadMedia === 'function') {" +
                " window._downloadMediaBackup = window.downloadMedia; }" +
                "window.downloadMedia = function(form, formData) {" +
                " try {" +
                "   const videoUrl = formData.get('url');" +
                "   const format = formData.get('format');" +
                "   if (!videoUrl || !format) { throw 'Missing url or format'; }" +
                "   app.downloadVideoLocally(videoUrl, format);" +
                "   window.location.href = HOME_URL;" +
                " } catch (error) {" +
                "   console.error('Local download failed', error);" +
                "   localStorage.setItem('downloadError', 'Local download failed: ' + error);" +
                "   window.location.href = HOME_URL;" +
                " }" +
                "};";

        engine.executeScript(script);
    }

    public static class DesktopDownloadBridge {

        private final DownloadService downloadService = new DownloadService();

        public void downloadVideoLocally(String videoUrl, String format) {
            new Thread(() -> {
                try {
                    if (!Files.exists(DOWNLOAD_FOLDER)) {
                        Files.createDirectories(DOWNLOAD_FOLDER);
                    }
                    downloadService.downloadVideoLocally(
                        videoUrl,
                        format,
                        DOWNLOAD_FOLDER.toString()
                    );
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Failed to download locally: " + e.getMessage(), 
                        "Download Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }, "LocalDownload").start();
        }
    }
    
}
