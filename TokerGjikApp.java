import javax.swing.*;
import java.awt.*;

/**
 * Tokerr Gjik - Modern Java Implementation
 * Now with modern CSS-like sty        // Create modern buttons with safe color decoding
        JButton newGameBtn = createModernButton("🔄 Lojë e Re / New Game", ACCENT_COLOR);
        JButton rulesBtn = createModernButton("📋 Rregullat / Rules", safeDecodeColor("#3498DB", new Color(52, 152, 219)));
        JButton exitBtn = createModernButton("🚪 Dil / Exit", safeDecodeColor("#95A5A6", new Color(149, 165, 166))); and proper game board centering
 * No CodeName One dependencies - works with standard Java
 * 
 * Game Rules:
 * - Each player has 9 pieces
 * - Game has 3 phases: Placement, Movement, Flying
 * - Goal is to form mills (3 pieces in a row) to capture opponent pieces
 * - Win by reducing opponent to 2 pieces or blocking all moves
 */
public class TokerGjikApp extends JFrame implements GameAppInterface {
    
    private GameEngine gameEngine;
    private GameBoard gameBoard;
    private JLabel statusLabel;
    private JLabel phaseLabel;
    private JLabel piecesLabel;
    private JLabel scoreLabel;
    private final ScoreManager scoreManager = new ScoreManager();
    private boolean winLogged = false;

    // Persistent AI toggle so new games keep the user's choice
    private JCheckBox aiToggle;

    
    // Modern CSS-like color scheme with production fallbacks
    private static final Color BACKGROUND_COLOR = safeDecodeColor("#2C3E50", new Color(44, 62, 80));   // Dark blue-gray
    private static final Color PANEL_COLOR = safeDecodeColor("#34495E", new Color(52, 73, 94));        // Slightly lighter
    private static final Color TEXT_COLOR = safeDecodeColor("#ECF0F1", new Color(236, 240, 241));      // Light gray
    private static final Color ACCENT_COLOR = safeDecodeColor("#E74C3C", new Color(231, 76, 60));      // Modern red
    
    // Safe color decoder with fallback for production environments
    private static Color safeDecodeColor(String hex, Color fallback) {
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            // Silently fall back to default color in production
            return fallback;
        }
    }
    
    public TokerGjikApp() {
        super("🎯 Tokerr Gjik - Loja Tradicionale Shqiptare / Albanian Traditional Game");
        gameEngine = new GameEngine();
        initializeModernUI();
        updateStatus();
    }
    
    private void initializeModernUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            // Fall back to system default look and feel
        }
        
        // Set dark theme colors
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Create centered game board
        gameBoard = new GameBoard(gameEngine, this);
        JPanel boardContainer = new JPanel(new BorderLayout());
        boardContainer.setBackground(BACKGROUND_COLOR);
        boardContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        boardContainer.add(gameBoard, BorderLayout.CENTER);
        
        // Create modern status panel
        JPanel statusPanel = createModernStatusPanel();
        
        // Create modern control panel
        JPanel controlPanel = createModernControlPanel();
        
        // Create menu bar
        createModernMenuBar();
        
        // Add components with proper layout
        add(statusPanel, BorderLayout.NORTH);
        add(boardContainer, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        // Set window properties - Mobile friendly sizing
        setResizable(true);
        pack();
        setLocationRelativeTo(null); // Center on screen
        
        // Set minimum size for better mobile experience
        setMinimumSize(new Dimension(700, 900));
        setPreferredSize(new Dimension(750, 950));
    }
    
    private JPanel createModernStatusPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Create status labels with modern styling  
        statusLabel = createStyledLabel("LOJTARI / PLAYER: 1", 16, Font.BOLD);
        phaseLabel = createStyledLabel("FAZË / PHASE: Vendosje / Placement", 14, Font.PLAIN);
        piecesLabel = createStyledLabel("COPAT / PIECES: P1(9+0) P2(9+0)", 14, Font.PLAIN);
        scoreLabel = createStyledLabel(scoreManager.formatScoreLine(), 14, Font.BOLD);
        
        panel.add(statusLabel);
        panel.add(phaseLabel);
        panel.add(piecesLabel);
        panel.add(scoreLabel);
        
        return panel;
    }
    
    private JLabel createStyledLabel(String text, int fontSize, int style) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font(Font.SANS_SERIF, style, fontSize));
        return label;
    }
    
    private JPanel createModernControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Create modern buttons
        JButton newGameBtn = createModernButton("LOJË E RE / NEW GAME", ACCENT_COLOR);
        JButton rulesBtn = createModernButton("RREGULLAT / RULES", Color.decode("#3498DB"));
        JButton exitBtn = createModernButton("DIL / EXIT", Color.decode("#95A5A6"));
        
        newGameBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startNewGame();
            }
        });
        rulesBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showRules();
            }
        });
        exitBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.exit(0);
            }
        });

        // Add AI toggle checkbox (non-modal control)
        this.aiToggle = new JCheckBox("Luaj kundër kompjuterit / Play vs AI");
        this.aiToggle.setSelected(false);
        this.aiToggle.setBackground(PANEL_COLOR);
        this.aiToggle.setForeground(TEXT_COLOR);
        this.aiToggle.setFocusPainted(false);
        this.aiToggle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                gameEngine.setAiEnabled(TokerGjikApp.this.aiToggle.isSelected());
                updateStatus();
            }
        });
        
        panel.add(newGameBtn);
        panel.add(rulesBtn);
        panel.add(exitBtn);
        panel.add(aiToggle);
        
        return panel;
    }
    
    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14)); // Larger font for mobile
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(12, 20, 12, 20) // Larger padding for mobile touch
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Set minimum size for better mobile experience
        button.setPreferredSize(new Dimension(180, 45));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void createModernMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PANEL_COLOR);
        menuBar.setBorder(BorderFactory.createLineBorder(TEXT_COLOR, 1));
        
        // Game menu
        JMenu gameMenu = new JMenu("*** Loja / Game");
        gameMenu.setForeground(TEXT_COLOR);
        
        JMenuItem newGame = new JMenuItem("🆕 Lojë e Re / New Game");
        JMenuItem resetScores = new JMenuItem("⭐ Reset Pikët / Reset Scores");
        JMenuItem exit = new JMenuItem("🚪 Dil / Exit");
        
        newGame.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startNewGame();
            }
        });
        resetScores.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                scoreManager.reset();
                if (scoreLabel != null) scoreLabel.setText(scoreManager.formatScoreLine());
            }
        });
        exit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.exit(0);
            }
        });
        
        gameMenu.add(newGame);
        gameMenu.add(resetScores);
        gameMenu.addSeparator();
        gameMenu.add(exit);
        
        // Help menu
        JMenu helpMenu = new JMenu("❓ Ndihmë / Help");
        helpMenu.setForeground(TEXT_COLOR);
        
        JMenuItem rules = new JMenuItem("📋 Rregullat / Rules");
        JMenuItem about = new JMenuItem("ℹ️ Rreth / About");
        
        rules.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showRules();
            }
        });
        about.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showAbout();
            }
        });
        
        helpMenu.add(rules);
        helpMenu.add(about);
        
        menuBar.add(gameMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void startNewGame() {
        gameEngine = new GameEngine();
        // Preserve AI setting across new games
        if (this.aiToggle != null) {
            gameEngine.setAiEnabled(this.aiToggle.isSelected());
        }
        gameBoard = new GameBoard(gameEngine, this);
        winLogged = false;
        updateStatus();
        
        // Refresh the board display
        Component[] components = ((JPanel)getContentPane().getComponent(1)).getComponents();
        if (components.length > 0) {
            ((JPanel)components[0]).removeAll();
            ((JPanel)components[0]).add(gameBoard, BorderLayout.CENTER);
            ((JPanel)components[0]).revalidate();
            ((JPanel)components[0]).repaint();
        }
    }
    
    // Method to refresh status when game state changes
    public void refreshGameState() {
        updateStatus();
        if (gameBoard != null) {
            gameBoard.updateBoard();
            // Force clear any lingering mill states after successful piece removal
            if (!gameEngine.wasMillFormed()) {
                gameBoard.clearRemovalState();
            }
        }
    }
    
    private void updateStatus() {
        if (statusLabel != null) {
            String currentPlayer = (gameEngine.getCurrentPlayer() == 1) ? "1 (RED)" : "2 (BLUE)";
            statusLabel.setText("LOJTARI / PLAYER: " + currentPlayer);
            
            String phase = "";
            switch (gameEngine.getCurrentPhase()) {
                case PLACEMENT:
                    phase = "Vendosje / Placement";
                    break;
                case MOVEMENT:
                    phase = "Lëvizje / Movement";
                    break;
                case FLYING:
                    phase = "Fluturim / Flying";
                    break;
            }
            
            // Check if mill was formed and show removal instruction
            if (gameEngine.wasMillFormed() && gameEngine.getRemovablePositions().length > 0) {
                phase += " - HEQ COPË / REMOVE PIECE";
                if (phaseLabel != null) {
                    phaseLabel.setForeground(Color.decode("#E74C3C")); // Red for attention
                }
            } else {
                if (phaseLabel != null) {
                    phaseLabel.setForeground(TEXT_COLOR); // Normal color
                }
            }
            
            phaseLabel.setText("FAZË / PHASE: " + phase);
            
            int[] remaining = gameEngine.getPiecesRemaining();
            int[] onBoard = gameEngine.getPiecesOnBoard();
            piecesLabel.setText(String.format("COPAT / PIECES: P1(%d+%d) P2(%d+%d)", 
                remaining[0], onBoard[0], remaining[1], onBoard[1]));
            if (scoreLabel != null) scoreLabel.setText(scoreManager.formatScoreLine());
            
            // Replace modal game-over dialog with status update and log (no message boxes)
            if (gameEngine.isGameOver()) {
                String winner = (gameEngine.getWinner() == 1) ? "Lojtari 1 (RED) / Player 1" : "Lojtari 2 (BLUE) / Player 2";
                // Update phase label and status label with non-blocking message
                phaseLabel.setText("FITUESI / WINNER: " + winner);
                statusLabel.setText(winner + " fitoi! / won! — Kliko 'Lojë e Re' për të luajtur përsëri.");
                if (!winLogged) {
                    scoreManager.addWinPoints(gameEngine.getWinner());
                    winLogged = true;
                    if (scoreLabel != null) scoreLabel.setText(scoreManager.formatScoreLine());
                }
                // Game completed - winner determined
            }
        }
    }
    
    // GameAppInterface implementation
    @Override
    public void updateGameStatus() {
        updateStatus();
    }
    
    @Override
    public void awardCapturePoints(int player) {
        if (player == 1) {
            scoreManager.addCapturePoints(1);
        }
    }
    
    @Override
    public void awardMillPoints(int player) {
        if (player == 1) {
            scoreManager.addMillPoints(1);
        }
    }
    
    private void showRules() {
        String rules = "📋 RREGULLAT E LOJËS / GAME RULES\n\n" +
                      "🎯 Objektivi / Objective:\n" +
                      "• Formo tre copa në vijë (mill) për të hequr copat e kundërshtarit\n" +
                      "• Form three pieces in a row (mill) to remove opponent's pieces\n\n" +
                      "🎮 Fazat e Lojës / Game Phases:\n\n" +
                      "1️⃣ VENDOSJA / PLACEMENT:\n" +
                      "• Secili lojtar vendos 9 copa një nga një\n" +
                      "• Each player places 9 pieces one by one\n" +
                      "• Kur formon mill, heq një copë të kundërshtarit\n" +
                      "• When forming a mill, remove one opponent piece\n\n" +
                      "2️⃣ LËVIZJA / MOVEMENT:\n" +
                      "• Lëviz copat në pozicione të lira të lidhura\n" +
                      "• Move pieces to connected free positions\n" +
                      "• Vazhdo të formosh mills\n" +
                      "• Continue forming mills\n\n" +
                      "3️⃣ FLUTURIMI / FLYING:\n" +
                      "• Kur ke vetëm 3 copa, mund të 'fluturosh' kudo\n" +
                      "• When you have only 3 pieces, you can 'fly' anywhere\n\n" +
                      "🏆 Fitimi / Winning:\n" +
                      "• Redukto kundërshtarin në 2 copa\n" +
                      "• Reduce opponent to 2 pieces\n" +
                      "• Ose blloko të gjitha lëvizjet e tij\n" +
                      "• Or block all their moves";
        
        // Non-blocking rules window (no modal message boxes)
        JFrame rulesFrame = new JFrame("📋 Rregullat / Rules");
        JTextArea textArea = new JTextArea(rules);
        textArea.setEditable(false);
        textArea.setBackground(PANEL_COLOR);
        textArea.setForeground(TEXT_COLOR);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(520, 420));
        rulesFrame.add(scrollPane);
        rulesFrame.pack();
        rulesFrame.setLocationRelativeTo(this);
        rulesFrame.setVisible(true);
    }
    
    private void showAbout() {
        // Replace modal dialog with a small non-blocking window
        JFrame aboutFrame = new JFrame("ℹ️ Rreth / About");
        JTextArea text = new JTextArea(
            "🎯 Tokerr Gjik\n" +
            "Lojë tradicionale shqiptare / Albanian traditional game\n\n" +
            "✨ Version: Modern Java Implementation\n" +
            "🎨 Features: CSS-like styling, centered board, modern UI\n" +
            "💻 No external dependencies required\n\n" +
            "© 2025 - Zhvilluar në Shqipëri / Developed in Albania"
        );
        text.setEditable(false);
        text.setBackground(PANEL_COLOR);
        text.setForeground(TEXT_COLOR);
        text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        text.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        aboutFrame.add(text);
        aboutFrame.pack();
        aboutFrame.setLocationRelativeTo(this);
        aboutFrame.setVisible(true);
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            // Fall back to system default look and feel
        }
        
        SwingUtilities.invokeLater(() -> {
            new TokerGjikApp().setVisible(true);
        });
    }
}
