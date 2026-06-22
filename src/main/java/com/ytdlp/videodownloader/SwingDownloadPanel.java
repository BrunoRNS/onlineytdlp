package com.ytdlp.videodownloader;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.ytdlp.videodownloader.service.DownloadService;

public class SwingDownloadPanel extends JPanel {

    private static final Path DOWNLOAD_FOLDER = Paths.get(System.getProperty("user.home"), "Downloads");
    private final DownloadService downloadService = new DownloadService();

    private JTextField urlField;
    private JRadioButton mp3Radio;
    private JRadioButton mp4Radio;
    private JLabel previewThumbnail;
    private JLabel previewTitle;
    private JLabel previewDuration;
    private JPanel previewPanel;
    private JButton downloadButton;
    private CardLayout cardLayout;
    private JPanel formPanel;
    private JPanel loadingPanel;

    public SwingDownloadPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(26, 26, 46));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        formPanel = createFormPanel();
        loadingPanel = createLoadingPanel();

        cardPanel.add(formPanel, "form");
        cardPanel.add(loadingPanel, "loading");
        add(cardPanel, BorderLayout.CENTER);

        JPanel footer = createFooter();
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logo = new JLabel("\uF167");
        logo.setFont(new Font("FontAwesome", Font.PLAIN, 48));
        logo.setForeground(new Color(255, 0, 0));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Yt-dlp Online Interface");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Use the yt-dlp API to download videos/audios from YouTube");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(170, 170, 170));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(20));
        panel.add(logo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(20));

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel urlLabel = new JLabel("YouTube URL:");
        urlLabel.setForeground(new Color(200, 200, 200));
        urlLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        urlLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(urlLabel);
        panel.add(Box.createVerticalStrut(5));

        urlField = new JTextField("https://www.youtube.com/watch?v=...");
        styleTextField(urlField);
        urlField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(urlField);
        panel.add(Box.createVerticalStrut(20));

        JLabel formatLabel = new JLabel("Select the format:");
        formatLabel.setForeground(new Color(200, 200, 200));
        formatLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        formatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(formatLabel);
        panel.add(Box.createVerticalStrut(10));

        ButtonGroup formatGroup = new ButtonGroup();
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        radioPanel.setOpaque(false);

        mp3Radio = createRadioButton("MP3 (Audio)", new Color(29, 185, 84));
        mp4Radio = createRadioButton("MP4 (Video)", new Color(255, 0, 0));
        mp4Radio.setSelected(true);

        formatGroup.add(mp3Radio);
        formatGroup.add(mp4Radio);
        radioPanel.add(mp3Radio);
        radioPanel.add(mp4Radio);
        radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(radioPanel);
        panel.add(Box.createVerticalStrut(20));

        previewPanel = createPreviewPanel();
        previewPanel.setVisible(false);
        previewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(previewPanel);
        panel.add(Box.createVerticalStrut(20));

        downloadButton = new JButton("Download");
        styleButton(downloadButton);
        downloadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        downloadButton.addActionListener(e -> startDownload());
        panel.add(downloadButton);

        return panel;
    }

    private JRadioButton createRadioButton(String text, Color accent) {
        JRadioButton radio = new JRadioButton(text);
        radio.setFont(new Font("SansSerif", Font.PLAIN, 14));
        radio.setForeground(Color.WHITE);
        radio.setOpaque(false);
        radio.setFocusPainted(false);
        radio.setIconTextGap(10);
        
        radio.addActionListener(e -> {
            if (radio.isSelected()) {
                radio.setBorder(BorderFactory.createLineBorder(accent, 2));
            } else {
                radio.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            }
        });

        radio.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        return radio;
    }

    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(0, 0, 0, 60));
        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1));
        panel.setPreferredSize(new Dimension(400, 200));

        previewThumbnail = new JLabel();
        previewThumbnail.setHorizontalAlignment(JLabel.CENTER);
        previewThumbnail.setPreferredSize(new Dimension(320, 180));
        panel.add(previewThumbnail, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        previewTitle = new JLabel("", JLabel.CENTER);
        previewTitle.setForeground(Color.WHITE);
        previewTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        previewDuration = new JLabel("", JLabel.CENTER);
        previewDuration.setForeground(new Color(170, 170, 170));
        previewDuration.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoPanel.add(previewTitle);
        infoPanel.add(previewDuration);
        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBackground(new Color(0, 0, 0, 50));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 20), 2),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
    }

    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JLabel loadingGif = new JLabel(new ImageIcon(getClass().getResource("/static/images/loading.gif")));
        loadingGif.setHorizontalAlignment(JLabel.CENTER);
        panel.add(loadingGif);
        return panel;
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setOpaque(false);
        JLabel copyright = new JLabel("© " + java.time.Year.now() + " yt-dlp online interface", JLabel.CENTER);
        copyright.setForeground(new Color(120, 120, 120));
        copyright.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JLabel developer = new JLabel("Developed by BrunoRNS", JLabel.CENTER);
        developer.setForeground(new Color(120, 120, 120));
        developer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(copyright);
        panel.add(developer);
        return panel;
    }

    private void startDownload() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a YouTube URL.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String format = mp3Radio.isSelected() ? "mp3" : "mp4";
        downloadButton.setEnabled(false);
        cardLayout.show(getParent(), "loading");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    if (!Files.exists(DOWNLOAD_FOLDER)) {
                        Files.createDirectories(DOWNLOAD_FOLDER);
                    }
                    downloadService.downloadVideoLocally(url, format, DOWNLOAD_FOLDER.toString());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(SwingDownloadPanel.this,
                            "Download failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                downloadButton.setEnabled(true);
                cardLayout.show(getParent(), "form");
            }
        }.execute();
    }
}