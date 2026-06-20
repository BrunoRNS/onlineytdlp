package com.ytdlp.videodownloader;

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
