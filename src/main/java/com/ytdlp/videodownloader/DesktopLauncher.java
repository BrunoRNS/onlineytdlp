package com.ytdlp.videodownloader;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class DesktopLauncher {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SpringApplication app = new SpringApplication(VideodownloaderApplication.class);
        app.setHeadless(false);
        ConfigurableApplicationContext context = app.run(args);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Yt-dlp Desktop");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            boolean is64 = System.getProperty("os.arch").contains("64");

            if (is64) {
                try {
                    Class<?> webViewPanelClass = Class.forName(
                            "com.ytdlp.videodownloader.WebViewPanel");
                    JPanel panel = (JPanel) webViewPanelClass
                            .getDeclaredConstructor().newInstance();
                    frame.add(panel, BorderLayout.CENTER);
                } catch (Exception e) {
                    frame.add(new SwingDownloadPanel(), BorderLayout.CENTER);
                }
            } else {
                frame.add(new SwingDownloadPanel(), BorderLayout.CENTER);
            }

            frame.pack();
            frame.setVisible(true);
        });
    }
}