package com.ytdlp.videodownloader;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class DesktopLauncher {

    public static void main(String[] args) {

        try {
            Thread springThread = new Thread(() -> {
                VideodownloaderApplication.main(args);
            }, "SpringBoot");
            springThread.setDaemon(true);
            springThread.start();

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new MainFrame();

                boolean is64 = System.getProperty("os.arch").contains("64");

                if (is64) {
                    frame.add(new WebViewPanel(), BorderLayout.CENTER);
                } else {
                    frame.add(new SwingDownloadPanel(), BorderLayout.CENTER);
                }
                frame.pack();
                frame.setVisible(true);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Error: " + e.getMessage() + "\n\nPlease contact the support team: https://github.com/BrunoRNS/onlineytdlp/issues",
                "ERROR, Failed to launch the application",
                JOptionPane.ERROR_MESSAGE,
                null
            );
            System.exit(-1);
        }

    }
    
}
