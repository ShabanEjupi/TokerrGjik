import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Leaderboard - Modern leaderboard system like mobile gaming apps
 * Features:
 * - Local player rankings
 * - ELO rating system
 * - Win/Loss statistics
 * - Beautiful mobile-style UI
 * - Trophy icons and achievements
 */
public class Leaderboard extends JDialog {
    private JPanel contentPanel;
    private JPanel playersPanel;
    private List<PlayerStats> playerStats;
    private Preferences prefs;
    
    // Modern colors matching the app theme
    private static final Color GRADIENT_START = new Color(79, 134, 247);
    private static final Color GRADIENT_END = new Color(51, 102, 187);
    private static final Color SURFACE_WHITE = new Color(255, 255, 255, 250);
    private static final Color GOLD_COLOR = new Color(255, 193, 7);
    private static final Color SILVER_COLOR = new Color(192, 192, 192);
    private static final Color BRONZE_COLOR = new Color(205, 127, 50);
    private static final Color TEXT_PRIMARY = new Color(28, 28, 30);
    private static final Color TEXT_SECONDARY = new Color(142, 142, 147);
    
    public Leaderboard(JFrame parent) {
        super(parent, "🏆 Leaderboard / Renditja", true);
        this.prefs = Preferences.userRoot().node("tokergjik_leaderboard");
        initializePlayerStats();
        setupUI();
        updateLeaderboard();
    }
    
    private void initializePlayerStats() {
        playerStats = new ArrayList<>();
        
        // Load existing stats or create realistic demo data with Albanian names
        String[] playerNames = {
            "Ardit", "Blerina", "Çlirim", "Drita", "Enes", "Fatjona", 
            "Genti", "Hana", "Ilir", "Jeta", "Kreshnik", "Ledia",
            "Mergim", "Nora", "Orest", "Pranvera", "Qemal", "Rita",
            "Skender", "Teuta", "Urim", "Vjollca", "Xhemajl", "Ylber", "Zana"
        };
        
        for (String name : playerNames) {
            PlayerStats stats = new PlayerStats();
            stats.name = name;
            stats.elo = prefs.getInt(name + "_elo", 1200 + (int)(Math.random() * 800));
            stats.wins = prefs.getInt(name + "_wins", (int)(Math.random() * 50));
            stats.losses = prefs.getInt(name + "_losses", (int)(Math.random() * 40));
            stats.gamesPlayed = stats.wins + stats.losses;
            stats.winRate = stats.gamesPlayed > 0 ? (stats.wins * 100 / stats.gamesPlayed) : 0;
            stats.countryFlag = getRandomFlag();
            playerStats.add(stats);
        }
        
        // Sort by ELO rating
        playerStats.sort((a, b) -> Integer.compare(b.elo, a.elo));
    }
    
    private String getRandomFlag() {
        String[] flags = {"🇦🇱", "🇺🇸", "🇬🇧", "🇩🇪", "🇫🇷", "🇮🇹", "🇪🇸", "🇨🇦"};
        return flags[(int)(Math.random() * flags.length)];
    }
    
    private void setupUI() {
        setSize(500, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Main panel with gradient background
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        contentPanel.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = createHeader();
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Players list
        playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setOpaque(false);
        playersPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JScrollPane scrollPane = new JScrollPane(playersPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with close button
        JPanel bottomPanel = createBottomPanel();
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(contentPanel);
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Title with trophy icon
        JLabel titleLabel = new JLabel("🏆 Leaderboard / Renditja", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Top Lojtarë / Top Players", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JButton closeButton = createModernButton("✖ Mbyll / Close", new Color(220, 53, 69));
        closeButton.addActionListener(evt -> dispose());
        
        JButton refreshButton = createModernButton("🔄 Rifresko / Refresh", new Color(40, 167, 69));
        refreshButton.addActionListener(evt -> {
            initializePlayerStats();
            updateLeaderboard();
        });
        
        bottomPanel.add(refreshButton);
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(closeButton);
        
        return bottomPanel;
    }
    
    private JButton createModernButton(String text, Color backgroundColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(backgroundColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(backgroundColor.brighter());
                } else {
                    g2d.setColor(backgroundColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(140, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void updateLeaderboard() {
        playersPanel.removeAll();
        
        for (int i = 0; i < playerStats.size(); i++) {
            PlayerStats stats = playerStats.get(i);
            JPanel playerCard = createPlayerCard(stats, i + 1);
            playersPanel.add(playerCard);
            
            if (i < playerStats.size() - 1) {
                playersPanel.add(Box.createVerticalStrut(8));
            }
        }
        
        playersPanel.revalidate();
        playersPanel.repaint();
    }
    
    private JPanel createPlayerCard(PlayerStats stats, int rank) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background with shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 15, 15);
                
                // Card surface
                g2d.setColor(SURFACE_WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 15, 15);
                
                g2d.dispose();
            }
        };
        
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(-1, 80));
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Left side - Rank and Player info
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        
        // Rank with trophy icon
        String rankIcon = getRankIcon(rank);
        JLabel rankLabel = new JLabel(rankIcon + " " + rank, JLabel.CENTER);
        rankLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        rankLabel.setForeground(getRankColor(rank));
        rankLabel.setPreferredSize(new Dimension(60, -1));
        
        // Player info
        JPanel playerInfo = new JPanel(new BorderLayout());
        playerInfo.setOpaque(false);
        
        JLabel nameLabel = new JLabel(stats.countryFlag + " " + stats.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_PRIMARY);
        
        JLabel statsLabel = new JLabel(String.format("ELO: %d • %d P / Games • %d%% Win Rate", 
                                                     stats.elo, stats.gamesPlayed, stats.winRate));
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(TEXT_SECONDARY);
        
        playerInfo.add(nameLabel, BorderLayout.NORTH);
        playerInfo.add(statsLabel, BorderLayout.SOUTH);
        
        leftPanel.add(rankLabel, BorderLayout.WEST);
        leftPanel.add(playerInfo, BorderLayout.CENTER);
        
        // Right side - ELO rating
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        
        JLabel eloLabel = new JLabel(String.valueOf(stats.elo), JLabel.CENTER);
        eloLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        eloLabel.setForeground(TEXT_PRIMARY);
        
        JLabel eloText = new JLabel("ELO", JLabel.CENTER);
        eloText.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        eloText.setForeground(TEXT_SECONDARY);
        
        rightPanel.add(eloLabel, BorderLayout.CENTER);
        rightPanel.add(eloText, BorderLayout.SOUTH);
        rightPanel.setPreferredSize(new Dimension(80, -1));
        
        card.add(leftPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private String getRankIcon(int rank) {
        switch (rank) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return "🏅";
        }
    }
    
    private Color getRankColor(int rank) {
        switch (rank) {
            case 1: return GOLD_COLOR;
            case 2: return SILVER_COLOR;
            case 3: return BRONZE_COLOR;
            default: return TEXT_SECONDARY;
        }
    }
    
    // Player statistics class
    private static class PlayerStats {
        String name;
        int elo;
        int wins;
        int losses;
        int gamesPlayed;
        int winRate;
        String countryFlag;
    }
    
    // Method to update player stats (called from game)
    public static void updatePlayerStats(String playerName, boolean won, int eloChange) {
        Preferences prefs = Preferences.userRoot().node("tokergjik_leaderboard");
        
        int currentElo = prefs.getInt(playerName + "_elo", 1200);
        int currentWins = prefs.getInt(playerName + "_wins", 0);
        int currentLosses = prefs.getInt(playerName + "_losses", 0);
        
        prefs.putInt(playerName + "_elo", currentElo + eloChange);
        
        if (won) {
            prefs.putInt(playerName + "_wins", currentWins + 1);
        } else {
            prefs.putInt(playerName + "_losses", currentLosses + 1);
        }
    }
}
