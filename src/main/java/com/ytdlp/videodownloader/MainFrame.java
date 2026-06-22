package com.ytdlp.videodownloader;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class MainFrame extends JFrame {

    public MainFrame() {
        this.setTitle("Online YTDLP - Video Downloader");
        this.setSize(800, 800);
        this.setResizable(false);
        this.setAutoRequestFocus(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
    }

}
