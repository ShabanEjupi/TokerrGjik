import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;

/**
 * SettingsManager - Comprehensive game settings management
 * Features:
 * - Sound and music controls
 * - Game difficulty settings
 * - Display preferences
 * - Language selection (Albanian/English)
 * - Player profile customization
 * - Performance settings
 */
public class SettingsManager {
    
    private static SettingsManager instance;
    private Properties settings;
    private static final String SETTINGS_FILE = "tokergjik_settings.properties";
    
    // Default settings
    private static final String DEFAULT_SOUND_ENABLED = "true";
    private static final String DEFAULT_MUSIC_ENABLED = "true";
    private static final String DEFAULT_SOUND_VOLUME = "0.7";
    private static final String DEFAULT_MUSIC_VOLUME = "0.3";
    private static final String DEFAULT_LANGUAGE = "albanian";
    private static final String DEFAULT_DIFFICULTY = "medium";
    private static final String DEFAULT_FULLSCREEN = "false";
    private static final String DEFAULT_ANIMATIONS = "true";
    private static final String DEFAULT_BOARD_THEME = "classic";
    private static final String DEFAULT_PLAYER_NAME = "Lojtar / Player";
    
    private SettingsManager() {
        settings = new Properties();
        loadSettings();
    }
    
    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }
    
    private void loadSettings() {
        try {
            File settingsFile = new File(SETTINGS_FILE);
            if (settingsFile.exists()) {
                FileInputStream fis = new FileInputStream(settingsFile);
                settings.load(fis);
                fis.close();
            } else {
                // Set default values
                setDefaultSettings();
                saveSettings();
            }
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            setDefaultSettings();
        }
    }
    
    private void setDefaultSettings() {
        settings.setProperty("sound.enabled", DEFAULT_SOUND_ENABLED);
        settings.setProperty("music.enabled", DEFAULT_MUSIC_ENABLED);
        settings.setProperty("sound.volume", DEFAULT_SOUND_VOLUME);
        settings.setProperty("music.volume", DEFAULT_MUSIC_VOLUME);
        settings.setProperty("game.language", DEFAULT_LANGUAGE);
        settings.setProperty("game.difficulty", DEFAULT_DIFFICULTY);
        settings.setProperty("display.fullscreen", DEFAULT_FULLSCREEN);
        settings.setProperty("display.animations", DEFAULT_ANIMATIONS);
        settings.setProperty("board.theme", DEFAULT_BOARD_THEME);
        settings.setProperty("player.name", DEFAULT_PLAYER_NAME);
    }
    
    public void saveSettings() {
        try {
            FileOutputStream fos = new FileOutputStream(SETTINGS_FILE);
            settings.store(fos, "TokerGjik Game Settings");
            fos.close();
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
    
    // Getters
    public boolean isSoundEnabled() {
        return Boolean.parseBoolean(settings.getProperty("sound.enabled", DEFAULT_SOUND_ENABLED));
    }
    
    public boolean isMusicEnabled() {
        return Boolean.parseBoolean(settings.getProperty("music.enabled", DEFAULT_MUSIC_ENABLED));
    }
    
    public float getSoundVolume() {
        return Float.parseFloat(settings.getProperty("sound.volume", DEFAULT_SOUND_VOLUME));
    }
    
    public float getMusicVolume() {
        return Float.parseFloat(settings.getProperty("music.volume", DEFAULT_MUSIC_VOLUME));
    }
    
    public String getLanguage() {
        return settings.getProperty("game.language", DEFAULT_LANGUAGE);
    }
    
    public String getDifficulty() {
        return settings.getProperty("game.difficulty", DEFAULT_DIFFICULTY);
    }
    
    public boolean isFullscreenEnabled() {
        return Boolean.parseBoolean(settings.getProperty("display.fullscreen", DEFAULT_FULLSCREEN));
    }
    
    public boolean areAnimationsEnabled() {
        return Boolean.parseBoolean(settings.getProperty("display.animations", DEFAULT_ANIMATIONS));
    }
    
    public String getBoardTheme() {
        return settings.getProperty("board.theme", DEFAULT_BOARD_THEME);
    }
    
    public String getPlayerName() {
        return settings.getProperty("player.name", DEFAULT_PLAYER_NAME);
    }
    
    // Setters
    public void setSoundEnabled(boolean enabled) {
        settings.setProperty("sound.enabled", String.valueOf(enabled));
        SoundManager.getInstance().setSoundEnabled(enabled);
    }
    
    public void setMusicEnabled(boolean enabled) {
        settings.setProperty("music.enabled", String.valueOf(enabled));
        // Background music no longer available - kept for settings compatibility
    }
    
    public void setSoundVolume(float volume) {
        settings.setProperty("sound.volume", String.valueOf(volume));
        SoundManager.getInstance().setMasterVolume(volume);
    }
    
    public void setMusicVolume(float volume) {
        settings.setProperty("music.volume", String.valueOf(volume));
        // Background music no longer available - kept for settings compatibility
    }
    
    public void setLanguage(String language) {
        settings.setProperty("game.language", language);
    }
    
    public void setDifficulty(String difficulty) {
        settings.setProperty("game.difficulty", difficulty);
    }
    
    public void setFullscreenEnabled(boolean enabled) {
        settings.setProperty("display.fullscreen", String.valueOf(enabled));
    }
    
    public void setAnimationsEnabled(boolean enabled) {
        settings.setProperty("display.animations", String.valueOf(enabled));
    }
    
    public void setBoardTheme(String theme) {
        settings.setProperty("board.theme", theme);
    }
    
    public void setPlayerName(String name) {
        settings.setProperty("player.name", name);
    }
    
    /**
     * Show settings dialog
     */
    public void showSettingsDialog(Component parent) {
        SwingUtilities.invokeLater(() -> {
            JDialog settingsDialog = createSettingsDialog(parent);
            settingsDialog.setVisible(true);
        });
    }
    
    private JDialog createSettingsDialog(Component parent) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), 
                                   "Rregullimet / Settings", true);
        dialog.setSize(500, 650);
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Create main panel with gradient background
        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("⚙️ Rregullimet e Lojës / Game Settings", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create settings tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Audio Settings Tab
        tabbedPane.addTab("🔊 Audio", createAudioSettingsPanel());
        
        // Game Settings Tab
        tabbedPane.addTab("🎮 Loja / Game", createGameSettingsPanel());
        
        // Display Settings Tab
        tabbedPane.addTab("🖥️ Ekrani / Display", createDisplaySettingsPanel());
        
        // Player Settings Tab
        tabbedPane.addTab("👤 Lojtari / Player", createPlayerSettingsPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        
        JButton saveButton = createStyledButton("✅ Ruaj / Save", new Color(52, 199, 89));
        JButton cancelButton = createStyledButton("❌ Anulo / Cancel", new Color(255, 59, 48));
        JButton resetButton = createStyledButton("🔄 Rivendos / Reset", new Color(255, 193, 7));
        
        saveButton.addActionListener(e -> {
            saveSettings();
            JOptionPane.showMessageDialog(dialog, "Rregullimet u ruajtën! / Settings saved!");
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        resetButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(dialog, 
                "A jeni i sigurt që dëshironi të rivendosni të gjitha rregullimet?\nAre you sure you want to reset all settings?", 
                "Konfirmo / Confirm", 
                JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                setDefaultSettings();
                dialog.dispose();
                showSettingsDialog(parent);
            }
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(resetButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        return dialog;
    }
    
    private JPanel createAudioSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);
        
        // Sound Effects
        JCheckBox soundEnabledBox = new JCheckBox("Aktivizo tingujt / Enable Sound Effects", isSoundEnabled());
        soundEnabledBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        soundEnabledBox.setOpaque(false);
        soundEnabledBox.setForeground(Color.WHITE);
        soundEnabledBox.addActionListener(e -> setSoundEnabled(soundEnabledBox.isSelected()));
        
        JSlider soundVolumeSlider = new JSlider(0, 100, (int)(getSoundVolume() * 100));
        soundVolumeSlider.addChangeListener(e -> setSoundVolume(soundVolumeSlider.getValue() / 100.0f));
        
        // Background Music
        JCheckBox musicEnabledBox = new JCheckBox("Aktivizo muzikën / Enable Background Music", isMusicEnabled());
        musicEnabledBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        musicEnabledBox.setOpaque(false);
        musicEnabledBox.setForeground(Color.WHITE);
        musicEnabledBox.addActionListener(e -> setMusicEnabled(musicEnabledBox.isSelected()));
        
        JSlider musicVolumeSlider = new JSlider(0, 100, (int)(getMusicVolume() * 100));
        musicVolumeSlider.addChangeListener(e -> setMusicVolume(musicVolumeSlider.getValue() / 100.0f));
        
        panel.add(soundEnabledBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(new JLabel("Volumi i tingujve / Sound Volume:"));
        panel.add(soundVolumeSlider);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(musicEnabledBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(new JLabel("Volumi i muzikës / Music Volume:"));
        panel.add(musicVolumeSlider);
        
        return panel;
    }
    
    private JPanel createGameSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);
        
        // Language Selection
        JLabel languageLabel = new JLabel("Gjuha / Language:");
        languageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        languageLabel.setForeground(Color.WHITE);
        
        String[] languages = {"albanian", "english", "bilingual"};
        String[] languageDisplays = {"🇦🇱 Shqip", "🇺🇸 English", "🌐 Të dyja / Both"};
        JComboBox<String> languageCombo = new JComboBox<>(languageDisplays);
        languageCombo.setSelectedIndex(java.util.Arrays.asList(languages).indexOf(getLanguage()));
        languageCombo.addActionListener(e -> setLanguage(languages[languageCombo.getSelectedIndex()]));
        
        // Difficulty Selection
        JLabel difficultyLabel = new JLabel("Vështirësia / Difficulty:");
        difficultyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        difficultyLabel.setForeground(Color.WHITE);
        
        String[] difficulties = {"easy", "medium", "hard", "expert"};
        String[] difficultyDisplays = {"😊 Lehtë / Easy", "😐 Mesatar / Medium", "😤 Vështirë / Hard", "🔥 Ekspert / Expert"};
        JComboBox<String> difficultyCombo = new JComboBox<>(difficultyDisplays);
        difficultyCombo.setSelectedIndex(java.util.Arrays.asList(difficulties).indexOf(getDifficulty()));
        difficultyCombo.addActionListener(e -> setDifficulty(difficulties[difficultyCombo.getSelectedIndex()]));
        
        // Board Theme Selection
        JLabel themeLabel = new JLabel("Tema e fushës / Board Theme:");
        themeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        themeLabel.setForeground(Color.WHITE);
        
        String[] themes = {"classic", "modern", "wooden", "metal"};
        String[] themeDisplays = {"🏛️ Klasike / Classic", "💎 Moderne / Modern", "🌳 Druri / Wooden", "⚙️ Metali / Metal"};
        JComboBox<String> themeCombo = new JComboBox<>(themeDisplays);
        themeCombo.setSelectedIndex(java.util.Arrays.asList(themes).indexOf(getBoardTheme()));
        themeCombo.addActionListener(e -> setBoardTheme(themes[themeCombo.getSelectedIndex()]));
        
        panel.add(languageLabel);
        panel.add(languageCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(difficultyLabel);
        panel.add(difficultyCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(themeLabel);
        panel.add(themeCombo);
        
        return panel;
    }
    
    private JPanel createDisplaySettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);
        
        JCheckBox fullscreenBox = new JCheckBox("Ekran i plotë / Fullscreen Mode", isFullscreenEnabled());
        fullscreenBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fullscreenBox.setOpaque(false);
        fullscreenBox.setForeground(Color.WHITE);
        fullscreenBox.addActionListener(e -> setFullscreenEnabled(fullscreenBox.isSelected()));
        
        JCheckBox animationsBox = new JCheckBox("Aktivizo animacionet / Enable Animations", areAnimationsEnabled());
        animationsBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        animationsBox.setOpaque(false);
        animationsBox.setForeground(Color.WHITE);
        animationsBox.addActionListener(e -> setAnimationsEnabled(animationsBox.isSelected()));
        
        panel.add(fullscreenBox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(animationsBox);
        
        return panel;
    }
    
    private JPanel createPlayerSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);
        
        JLabel nameLabel = new JLabel("Emri i lojtarit / Player Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        
        JTextField nameField = new JTextField(getPlayerName());
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.addActionListener(e -> setPlayerName(nameField.getText()));
        
        panel.add(nameLabel);
        panel.add(nameField);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    // Gradient panel for beautiful background
    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            GradientPaint gradient = new GradientPaint(0, 0, new Color(79, 134, 247), 
                                                      0, getHeight(), new Color(51, 102, 187));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
