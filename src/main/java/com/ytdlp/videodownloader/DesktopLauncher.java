package com.ytdlp.videodownloader;

import com.ytdlp.videodownloader.controller.DownloadController;
import com.ytdlp.videodownloader.service.DownloadService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;


public class DesktopLauncher extends JFrame {
    
    private static ConfigurableApplicationContext springContext;
    
    private final JTextField txtUrl;
    private final JComboBox<String> cbFormat;
    private final JTextField txtFolder;
    private final JButton btnBrowse;
    private final JButton btnDownload;
    private final JProgressBar progressBar;

    public DesktopLauncher() {

        setTitle("YT-DLP Video Downloader Desktop");
        setSize(540, 290);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(18, 18, 18, 18));
        add(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.0;
        mainPanel.add(new JLabel("URL do Vídeo:"), gbc);

        txtUrl = new JTextField();
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        mainPanel.add(txtUrl, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Formato:"), gbc);

        cbFormat = new JComboBox<>(new String[]{"mp4", "mp3"});
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        mainPanel.add(cbFormat, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Salvar em:"), gbc);

        String defaultDownloadDir = System.getProperty("user.home") + File.separator + "Downloads";
        txtFolder = new JTextField(defaultDownloadDir);
        txtFolder.setEditable(false);
        gbc.gridx = 1; gbc.weightx = 1.0;
        mainPanel.add(txtFolder, gbc);

        btnBrowse = new JButton("...");
        btnBrowse.setToolTipText("Escolher diretório de destino");
        gbc.gridx = 2; gbc.weightx = 0.0;
        mainPanel.add(btnBrowse, gbc);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Aguardando inserção de link...");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3; gbc.weightx = 1.0;
        mainPanel.add(progressBar, gbc);

        btnDownload = new JButton("Iniciar Download Nativo");
        btnDownload.setFont(btnDownload.getFont().deriveFont(Font.BOLD, 13f));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3; gbc.weightx = 1.0;
        mainPanel.add(btnDownload, gbc);

        initEventBindings();

    }

    private void initEventBindings() {

        btnBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(txtFolder.getText()));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Selecione a pasta de destino final");
            
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtFolder.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnDownload.addActionListener(e -> processarFluxoDownloadExecutável());
    }

    private void processarFluxoDownloadExecutável() {
        String urlRaw = txtUrl.getText().trim();
        String formatoEscolhido = (String) cbFormat.getSelectedItem();
        String pastaDestino = txtFolder.getText();

        String urlNormalizada;
        try {
            urlNormalizada = DownloadController.isValidYouTubeUrl(urlRaw);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, 
                    "Link Inválido: " + ex.getMessage(), 
                    "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        bloquearComponentesInterface(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Aguardando retorno do processo yt-dlp...");

        final String urlFinalValidada = urlNormalizada;

        new Thread(() -> {
            try {
                DownloadService downloadService = springContext.getBean(DownloadService.class);
                
                downloadService.downloadVideoLocally(urlFinalValidada, formatoEscolhido, pastaDestino);

                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    progressBar.setString("Download Concluído com Sucesso!");
                    JOptionPane.showMessageDialog(this, 
                            "Mídia baixada e processada na pasta selecionada!", 
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    limparEMesclarInterface();
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                    progressBar.setString("Falha na execução.");
                    JOptionPane.showMessageDialog(this, 
                            "Erro crítico durante o download:\n" + ex.getMessage(), 
                            "Erro de Execução", JOptionPane.ERROR_MESSAGE);
                    bloquearComponentesInterface(true);
                });
            }
        }).start();
    }

    private void bloquearComponentesInterface(boolean habilitado) {
        txtUrl.setEnabled(habilitado);
        cbFormat.setEnabled(habilitado);
        btnBrowse.setEnabled(habilitado);
        btnDownload.setEnabled(habilitado);
    }

    private void limparEMesclarInterface() {
        txtUrl.setText("");
        progressBar.setValue(0);
        progressBar.setString("Aguardando inserção de link...");
        bloquearComponentesInterface(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DesktopLauncher app = new DesktopLauncher();
            app.setVisible(true);
        });

        springContext = new SpringApplicationBuilder()
                .sources(VideodownloaderApplication.class)
                .headless(false) 
                .run(args);
    }
}
