package com.ytdlp.videodownloader;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;

import javafx.embed.swing.JFXPanel;

public class MainFrame extends JFrame {

    public MainFrame() {
        this.setTitle("Online YTDLP - Video Downloader");
        this.setSize(800, 800);
        this.setResizable(false);
        this.setAutoRequestFocus(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout());

        JFXPanel mainPanel = new WebViewPanel();
        mainPanel.setPreferredSize(new Dimension(800, 800));

        this.add(mainPanel);
        this.pack();
    }

}
