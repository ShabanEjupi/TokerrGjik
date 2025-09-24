/**
 * GameBoard.java - Modern Game Board Implementation
 * Compatible with standard Java (no CodeName One dependency)
 * Modern CSS-like styling with proper centering
 */

import java.awt.*;
import javax.swing.*;

public class GameBoard extends JPanel {
    private GameEngine gameEngine;
    private GameAppInterface gameApp;
    private static final int BASE_BOARD_SIZE = 600;
    private static final int PIECE_SIZE = 30; // Larger for mobile
    private static final int POSITION_SIZE = 16; // Larger for mobile
    
    // Dynamic sizing variables
    private int boardSize = BASE_BOARD_SIZE;
    private int offsetX = 0;
    private int offsetY = 0;
    
    // Modern color palette - CSS-like styling with fallbacks for production
    private static final Color BACKGROUND_COLOR = safeDecodeColor("#2C3E50", new Color(44, 62, 80));   // Dark blue-gray
    private static final Color BOARD_COLOR = safeDecodeColor("#34495E", new Color(52, 73, 94));        // Slightly lighter
    private static final Color LINE_COLOR = safeDecodeColor("#ECF0F1", new Color(236, 240, 241));      // Light gray
    private static final Color PLAYER1_COLOR = safeDecodeColor("#E74C3C", new Color(231, 76, 60));     // Modern red
    private static final Color PLAYER2_COLOR = safeDecodeColor("#3498DB", new Color(52, 152, 219));    // Modern blue
    private static final Color SELECTED_COLOR = safeDecodeColor("#F39C12", new Color(243, 156, 18));   // Orange highlight
    private static final Color POSITION_COLOR = safeDecodeColor("#BDC3C7", new Color(189, 195, 199));  // Light gray
    private static final Color HOVER_COLOR = safeDecodeColor("#F1C40F", new Color(241, 196, 15));      // Yellow hover
    
    // Safe color decoder with fallback for production environments
    private static Color safeDecodeColor(String hex, Color fallback) {
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            System.out.println("Warning: Could not decode color " + hex + ", using fallback");
            return fallback;
        }
    }
    
    // Dynamic positions that will be calculated based on component size
    private Point[] positions = new Point[24];
    
    private int hoveredPosition = -1;
    private boolean awaitingRemoval = false;
    private int[] removablePositions = new int[0];
    
    public GameBoard(GameEngine engine) {
        this.gameEngine = engine;
        this.gameApp = null;
        initializeBoard();
    }
    
    public GameBoard(GameEngine engine, GameAppInterface gameInterface) {
        this.gameEngine = engine;
        this.gameApp = gameInterface;
        initializeBoard();
    }
    
    private void initializeBoard() {
        setPreferredSize(new Dimension(BASE_BOARD_SIZE, BASE_BOARD_SIZE));
        setBackground(BACKGROUND_COLOR);
        calculateBoardDimensions();
        
        // Add mouse listeners for modern interaction
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
        
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
        });
        
        // Add component listener to handle resizing
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                calculateBoardDimensions();
                repaint();
            }
        });
    }
    

    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Always recalculate board dimensions to handle resizing
        calculateBoardDimensions();
        
        // Enable anti-aliasing for smooth graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Draw board with gradient background
        drawModernBackground(g2d);
        drawBoard(g2d);
        drawPieces(g2d);
        
        g2d.dispose();
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        // Recalculate positions when bounds change
        calculateBoardDimensions();
        repaint();
    }
    
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        // Recalculate positions when size changes
        calculateBoardDimensions();
        repaint();
    }
    
    private void calculateBoardDimensions() {
        int width = getWidth();
        int height = getHeight();
        
        // Ensure we have valid dimensions
        if (width <= 0 || height <= 0) {
            width = BASE_BOARD_SIZE;
            height = BASE_BOARD_SIZE;
        }
        
        // Calculate the maximum board size that fits in the component with padding
        int padding = 60; // Reasonable padding for all screen sizes
        boardSize = Math.min(width - padding, height - padding);
        
        // Ensure minimum size but allow scaling for all screen sizes
        if (boardSize < 300) {
            boardSize = 300;
        }
        
        // Perfect centering calculation for all screen sizes including fullscreen
        offsetX = (width - boardSize) / 2;
        offsetY = (height - boardSize) / 2;
        
        // Additional centering adjustments for ultra-wide screens
        if (width > height * 2) {
            // For very wide screens, use a reasonable max size
            int maxBoardSize = Math.min(height - padding, 800);
            boardSize = Math.min(boardSize, maxBoardSize);
            offsetX = (width - boardSize) / 2;
            offsetY = (height - boardSize) / 2;
        }
        
        // Calculate all 24 positions based on the dynamic board size
        int margin = boardSize / 8; // Smaller margin for better fit
        int outerSize = boardSize - margin * 2;
        int middleSize = (outerSize * 2) / 3;
        int innerSize = outerSize / 3;
        
        int centerX = offsetX + boardSize / 2;
        int centerY = offsetY + boardSize / 2;
        
        // Outer square positions
        positions[0] = new Point(centerX - outerSize/2, centerY - outerSize/2);  // 0 - top left
        positions[1] = new Point(centerX, centerY - outerSize/2);               // 1 - top center
        positions[2] = new Point(centerX + outerSize/2, centerY - outerSize/2); // 2 - top right
        
        // Middle square positions
        positions[3] = new Point(centerX - middleSize/2, centerY - middleSize/2);  // 3 - middle top left
        positions[4] = new Point(centerX, centerY - middleSize/2);                // 4 - middle top center
        positions[5] = new Point(centerX + middleSize/2, centerY - middleSize/2); // 5 - middle top right
        
        // Inner square positions
        positions[6] = new Point(centerX - innerSize/2, centerY - innerSize/2);  // 6 - inner top left
        positions[7] = new Point(centerX, centerY - innerSize/2);               // 7 - inner top center
        positions[8] = new Point(centerX + innerSize/2, centerY - innerSize/2); // 8 - inner top right
        
        // Left column
        positions[9] = new Point(centerX - outerSize/2, centerY);  // 9 - outer left
        positions[10] = new Point(centerX - middleSize/2, centerY); // 10 - middle left
        positions[11] = new Point(centerX - innerSize/2, centerY);  // 11 - inner left
        
        // Right column
        positions[12] = new Point(centerX + innerSize/2, centerY);  // 12 - inner right
        positions[13] = new Point(centerX + middleSize/2, centerY); // 13 - middle right
        positions[14] = new Point(centerX + outerSize/2, centerY);  // 14 - outer right
        
        // Inner square bottom
        positions[15] = new Point(centerX - innerSize/2, centerY + innerSize/2);  // 15 - inner bottom left
        positions[16] = new Point(centerX, centerY + innerSize/2);               // 16 - inner bottom center
        positions[17] = new Point(centerX + innerSize/2, centerY + innerSize/2); // 17 - inner bottom right
        
        // Middle square bottom
        positions[18] = new Point(centerX - middleSize/2, centerY + middleSize/2);  // 18 - middle bottom left
        positions[19] = new Point(centerX, centerY + middleSize/2);                // 19 - middle bottom center
        positions[20] = new Point(centerX + middleSize/2, centerY + middleSize/2); // 20 - middle bottom right
        
        // Outer square bottom
        positions[21] = new Point(centerX - outerSize/2, centerY + outerSize/2);  // 21 - outer bottom left
        positions[22] = new Point(centerX, centerY + outerSize/2);               // 22 - outer bottom center
        positions[23] = new Point(centerX + outerSize/2, centerY + outerSize/2); // 23 - outer bottom right
    }
    
    private void drawModernBackground(Graphics2D g2d) {        
        // Ensure we have valid dimensions
        int width = Math.max(getWidth(), 1);
        int height = Math.max(getHeight(), 1);
        
        // Create gradient background - fallback to solid color if gradient fails
        try {
            GradientPaint gradient = new GradientPaint(
                0, 0, BACKGROUND_COLOR,
                width, height, BOARD_COLOR
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width, height);
        } catch (Exception e) {
            // Fallback to solid background
            g2d.setColor(BACKGROUND_COLOR);
            g2d.fillRect(0, 0, width, height);
        }
        
        // Add subtle border with improved visibility
        g2d.setColor(LINE_COLOR);
        g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (width > 20 && height > 20) {
            g2d.drawRoundRect(10, 10, width-20, height-20, 20, 20);
        }
    }
    
    private void drawBoard(Graphics2D g2d) {
        if (positions[0] == null) return; // Not initialized yet
        
        g2d.setColor(LINE_COLOR);
        g2d.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Thicker lines for mobile
        
        // Draw outer square (positions 0, 1, 2, 14, 23, 22, 21, 9)
        g2d.drawLine(positions[0].x, positions[0].y, positions[2].x, positions[2].y); // top
        g2d.drawLine(positions[2].x, positions[2].y, positions[14].x, positions[14].y); // right top
        g2d.drawLine(positions[14].x, positions[14].y, positions[23].x, positions[23].y); // right bottom
        g2d.drawLine(positions[23].x, positions[23].y, positions[21].x, positions[21].y); // bottom
        g2d.drawLine(positions[21].x, positions[21].y, positions[9].x, positions[9].y); // left bottom
        g2d.drawLine(positions[9].x, positions[9].y, positions[0].x, positions[0].y); // left top
        
        // Draw middle square (positions 3, 4, 5, 13, 20, 19, 18, 10)
        g2d.drawLine(positions[3].x, positions[3].y, positions[5].x, positions[5].y); // top
        g2d.drawLine(positions[5].x, positions[5].y, positions[13].x, positions[13].y); // right top
        g2d.drawLine(positions[13].x, positions[13].y, positions[20].x, positions[20].y); // right bottom
        g2d.drawLine(positions[20].x, positions[20].y, positions[18].x, positions[18].y); // bottom
        g2d.drawLine(positions[18].x, positions[18].y, positions[10].x, positions[10].y); // left bottom
        g2d.drawLine(positions[10].x, positions[10].y, positions[3].x, positions[3].y); // left top
        
        // Draw inner square (positions 6, 7, 8, 12, 17, 16, 15, 11)
        g2d.drawLine(positions[6].x, positions[6].y, positions[8].x, positions[8].y); // top
        g2d.drawLine(positions[8].x, positions[8].y, positions[12].x, positions[12].y); // right top
        g2d.drawLine(positions[12].x, positions[12].y, positions[17].x, positions[17].y); // right bottom
        g2d.drawLine(positions[17].x, positions[17].y, positions[15].x, positions[15].y); // bottom
        g2d.drawLine(positions[15].x, positions[15].y, positions[11].x, positions[11].y); // left bottom
        g2d.drawLine(positions[11].x, positions[11].y, positions[6].x, positions[6].y); // left top
        
        // Draw connecting lines
        g2d.setStroke(new BasicStroke(3f)); // Thicker connecting lines
        // Vertical connections
        g2d.drawLine(positions[1].x, positions[1].y, positions[7].x, positions[7].y); // top center
        g2d.drawLine(positions[16].x, positions[16].y, positions[22].x, positions[22].y); // bottom center
        // Horizontal connections
        g2d.drawLine(positions[9].x, positions[9].y, positions[11].x, positions[11].y); // left center
        g2d.drawLine(positions[12].x, positions[12].y, positions[14].x, positions[14].y); // right center
        
        // Draw position markers with modern styling
        drawPositionMarkers(g2d);
    }
    
    private void drawPositionMarkers(Graphics2D g2d) {
        for (int i = 0; i < positions.length; i++) {
            Point pos = positions[i];
            
            // Choose color based on state
            Color markerColor = POSITION_COLOR;
            if (i == hoveredPosition) {
                markerColor = HOVER_COLOR;
            }
            
            // Draw position with subtle shadow effect (larger for mobile)
            g2d.setColor(Color.BLACK);
            g2d.fillOval(pos.x - POSITION_SIZE/2 + 2, pos.y - POSITION_SIZE/2 + 2, 
                        POSITION_SIZE + 2, POSITION_SIZE + 2);
            
            g2d.setColor(markerColor);
            g2d.fillOval(pos.x - POSITION_SIZE/2, pos.y - POSITION_SIZE/2, 
                        POSITION_SIZE, POSITION_SIZE);
            
            // Add position number for debugging (larger and more visible for mobile)
            g2d.setColor(new Color(200, 200, 200, 150)); // More visible but still subtle
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11)); // Slightly larger for mobile
            String num = String.valueOf(i);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(num, pos.x - fm.stringWidth(num)/2, pos.y + fm.getAscent()/2);
        }
    }
    
    private void drawPieces(Graphics2D g2d) {
        if (gameEngine == null) return;
        
        int[] board = gameEngine.getBoard();
        int selectedPos = gameEngine.getSelectedPosition();
        
        for (int i = 0; i < Math.min(board.length, positions.length); i++) {
            if (board[i] != GameEngine.EMPTY) {
                Point pos = positions[i];
                
                // Choose piece color
                Color pieceColor = (board[i] == GameEngine.PLAYER_1) ? PLAYER1_COLOR : PLAYER2_COLOR;
                
                if (i == selectedPos) {
                    pieceColor = SELECTED_COLOR; // Highlight selected piece
                } else if (awaitingRemoval && isRemovablePosition(i)) {
                    // Highlight removable pieces with red-orange color
                    pieceColor = Color.decode("#FF6B35"); // Orange-red to indicate removable
                }
                
                // Draw piece with modern 3D effect
                drawModernPiece(g2d, pos.x, pos.y, pieceColor, board[i]);
                
                // Add extra visual feedback for removable pieces
                if (awaitingRemoval && isRemovablePosition(i)) {
                    // Draw pulsing border around removable pieces
                    g2d.setColor(Color.decode("#E74C3C")); // Red border
                    g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawOval(pos.x - PIECE_SIZE - 3, pos.y - PIECE_SIZE - 3, 
                               (PIECE_SIZE + 3) * 2, (PIECE_SIZE + 3) * 2);
                }
            }
        }
    }
    
    private void drawModernPiece(Graphics2D g2d, int x, int y, Color color, int player) {
        // Fallback rendering for production mode compatibility
        try {
            // Create gradient for 3D effect
            RadialGradientPaint gradient = new RadialGradientPaint(
                x - PIECE_SIZE/4, y - PIECE_SIZE/4, PIECE_SIZE + 5,
                new float[]{0.0f, 0.7f, 1.0f},
                new Color[]{brighter(color), color, darker(color)}
            );
            g2d.setPaint(gradient);
        } catch (Exception e) {
            // Fallback to solid color in production mode
            g2d.setColor(color);
        }
        
        // Draw shadow (larger for mobile)
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillOval(x - PIECE_SIZE/2 + 2, y - PIECE_SIZE/2 + 2, PIECE_SIZE + 6, PIECE_SIZE + 6);
        
        // Draw piece with gradient or solid color (larger for mobile)
        try {
            RadialGradientPaint gradient = new RadialGradientPaint(
                x - PIECE_SIZE/4, y - PIECE_SIZE/4, PIECE_SIZE + 5,
                new float[]{0.0f, 0.7f, 1.0f},
                new Color[]{brighter(color), color, darker(color)}
            );
            g2d.setPaint(gradient);
        } catch (Exception e) {
            g2d.setColor(color);
        }
        g2d.fillOval(x - PIECE_SIZE/2, y - PIECE_SIZE/2, PIECE_SIZE + 4, PIECE_SIZE + 4);
        
        // Draw thicker border for mobile - always use solid colors for borders
        g2d.setColor(darker(color));
        g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawOval(x - PIECE_SIZE/2, y - PIECE_SIZE/2, PIECE_SIZE + 4, PIECE_SIZE + 4);
        
        // Draw player number with larger, bold typography for mobile
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18)); // Even larger font for better visibility
        String playerNum = String.valueOf(player);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x - fm.stringWidth(playerNum) / 2;
        int textY = y + fm.getAscent() / 2 - 1;
        
        // Draw text shadow (stronger for mobile)
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.drawString(playerNum, textX + 2, textY + 2);
        
        // Draw text
        g2d.setColor(Color.WHITE);
        g2d.drawString(playerNum, textX, textY);
    }
    
    private Color brighter(Color color) {
        return new Color(
            Math.min(255, (int)(color.getRed() * 1.3)),
            Math.min(255, (int)(color.getGreen() * 1.3)),
            Math.min(255, (int)(color.getBlue() * 1.3)),
            color.getAlpha()
        );
    }
    
    private Color darker(Color color) {
        return new Color(
            (int)(color.getRed() * 0.7),
            (int)(color.getGreen() * 0.7),
            (int)(color.getBlue() * 0.7),
            color.getAlpha()
        );
    }
    
    private void handleClick(int x, int y) {
        int closestPosition = findClosestPosition(x, y);
        if (closestPosition >= 0) {
            System.out.println("Clicked position: " + closestPosition + ", awaiting removal: " + awaitingRemoval);
            
            if (awaitingRemoval) {
                // Player needs to remove an opponent piece after forming a mill
                System.out.println("Removable positions: " + java.util.Arrays.toString(removablePositions));
                
                if (isRemovablePosition(closestPosition)) {
                    System.out.println("Attempting to remove piece at position: " + closestPosition);
                    boolean removed = gameEngine.removePiece(closestPosition);
                    if (removed) {
                        // IMMEDIATELY clear removal state to prevent further confusion
                        awaitingRemoval = false;
                        removablePositions = new int[0];
                        
                        // Show success message with visual feedback
                        showMessage("✅ Copë e hequr! Tani luan lojtari " + gameEngine.getCurrentPlayer() + " / Piece removed! Now player " + gameEngine.getCurrentPlayer() + " plays!");

                        // Award capture points to the player who removed (previous player after switch)
                        if (gameApp != null) {
                            int remover = (gameEngine.getCurrentPlayer() == GameEngine.PLAYER_1) ? GameEngine.PLAYER_2 : GameEngine.PLAYER_1;
                            gameApp.awardCapturePoints(remover);
                        }
                        
                        // Play a visual effect for successful removal
                        showRemovalEffect(closestPosition);
                        
                        // Force complete game state refresh
                        System.out.println("After removal: Current player = " + gameEngine.getCurrentPlayer() + ", Phase = " + gameEngine.getCurrentPhase());
                        
                        // Ensure the mill flag is completely cleared
                        if (gameEngine.wasMillFormed()) {
                            System.out.println("WARNING: Mill flag still set after removal - this should not happen!");
                        }
                    } else {
                        // Show error message
                        showMessage("❌ Nuk mund ta heqësh atë copë! / Cannot remove that piece!");
                    }
                } else {
                    // Not a removable position - check what's at this position
                    int[] board = gameEngine.getBoard();
                    int opponent = (gameEngine.getCurrentPlayer() == GameEngine.PLAYER_1) ? GameEngine.PLAYER_2 : GameEngine.PLAYER_1;
                    if (board[closestPosition] == opponent) {
                        showMessage("⚠️ Ajo copë është në mill dhe nuk mund të hiqet! / That piece is in a mill and cannot be removed!");
                    } else if (board[closestPosition] == gameEngine.getCurrentPlayer()) {
                        showMessage("⚠️ Nuk mund të heqësh copët e tua! / You cannot remove your own pieces!");
                    } else {
                        showMessage("⚠️ Zgjedh një copë të kundërshtarit për të hequr! / Choose an opponent's piece to remove!");
                    }
                }
            } else {
                // Normal move
                boolean moveSuccessful = gameEngine.handlePositionClick(closestPosition);
                if (moveSuccessful) {
                    System.out.println("Move successful, checking for mill...");
                    
                    if (gameEngine.wasMillFormed()) {
                        System.out.println("Mill was formed!");
                        // Mill was formed - player needs to remove opponent piece
                        awaitingRemoval = true;
                        removablePositions = gameEngine.getRemovablePositions();
                        
                        // Award mill points to the player who formed it
                        if (gameApp != null) {
                            gameApp.awardMillPoints(gameEngine.getCurrentPlayer());
                        }
                        
                        System.out.println("Removable positions after mill: " + java.util.Arrays.toString(removablePositions));
                        
                        // Show instruction message with visual feedback
                        showMessage("🎯 Mill u formua! Hiq një copë të kundërshtarit! / Mill formed! Remove an opponent piece!");
                        
                        // If no removable pieces, automatically skip removal
                        if (removablePositions.length == 0) {
                            awaitingRemoval = false;
                            showMessage("ℹ️ Asnjë copë s'mund të hiqet! Tani luan lojtari " + gameEngine.getCurrentPlayer() + " / No pieces can be removed! Now player " + gameEngine.getCurrentPlayer() + " plays!");
                            // Force switch player since we can't remove anything
                            gameEngine.removePiece(-1); // This will clear the mill flag and switch player
                            System.out.println("No pieces removable - switched to player: " + gameEngine.getCurrentPlayer());
                        }
                    }
                } else {
                    // Move was not successful, provide feedback
                    if (gameEngine.getCurrentPhase() == GameEngine.GamePhase.PLACEMENT) {
                        if (closestPosition >= 0 && gameEngine.getBoard()[closestPosition] != GameEngine.EMPTY) {
                            showMessage("⚠️ Pozicioni është i zënë! / Position is occupied!");
                        } else if (gameEngine.getPiecesRemaining()[gameEngine.getCurrentPlayer() - 1] <= 0) {
                            showMessage("⚠️ Nuk ke më copa për të vendosur! / No more pieces to place!");
                        }
                    } else {
                        showMessage("⚠️ Lëvizje e pavlefshme! / Invalid move!");
                    }
                }
            }
            
            // Always update parent app status and force UI refresh
            if (gameApp != null) {
                SwingUtilities.invokeLater(() -> {
                    gameApp.updateGameStatus();
                    
                    // Check if AI should make a move
                    if (gameEngine.isAiEnabled() && gameEngine.isAiPlayer() && !gameEngine.isGameOver() && !awaitingRemoval) {
                        // Delay AI move slightly for better UX
                        javax.swing.Timer aiTimer = new javax.swing.Timer(800, new java.awt.event.ActionListener() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                if (gameEngine.makeAiMove()) {
                                    repaint();
                                    gameApp.updateGameStatus();
                                }
                            }
                        });
                         aiTimer.setRepeats(false);
                         aiTimer.start();
                    }
                });
            }
            
            // Force immediate repaint
            repaint();
            
            // Ensure game state is properly synchronized
            System.out.println("Move completed - Current player: " + gameEngine.getCurrentPlayer() + 
                             ", Phase: " + gameEngine.getCurrentPhase() + 
                             ", Awaiting removal: " + awaitingRemoval);
        }
    }
    
    private void showMessage(String message) {
        System.out.println(message);
        // Show visual feedback on the board only - no dialog boxes
        showStatusMessage(message);
    }
    
    private void showStatusMessage(String message) {
        // This could be enhanced with a timer to show temporary messages
        // For now, just trigger a repaint to update visual state
        repaint();
    }
    
    private void showRemovalEffect(int position) {
        // Visual effect for successful piece removal
        // This could be enhanced with animation in the future
        System.out.println("Piece removed from position: " + position);
    }
    
    private void handleMouseMove(int x, int y) {
        int newHovered = findClosestPosition(x, y);
        if (newHovered != hoveredPosition) {
            hoveredPosition = newHovered;
            repaint();
        }
    }
    
    private int findClosestPosition(int x, int y) {
        int closestPosition = -1;
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < positions.length; i++) {
            Point pos = positions[i];
            double distance = Math.sqrt(Math.pow(x - pos.x, 2) + Math.pow(y - pos.y, 2));
            if (distance < 45 && distance < minDistance) { // 45px touch radius for better mobile experience
                minDistance = distance;
                closestPosition = i;
            }
        }
        
        return closestPosition;
    }
    
    // Helper method to check if a position is removable
    private boolean isRemovablePosition(int position) {
        for (int removablePos : removablePositions) {
            if (removablePos == position) {
                return true;
            }
        }
        return false;
    }
    
    // Update method for external calls
    public void updateBoard() {
        awaitingRemoval = false;
        removablePositions = new int[0];
        hoveredPosition = -1;
        repaint();
    }
    
    // Force clear removal state - called when mill handling is complete
    public void clearRemovalState() {
        awaitingRemoval = false;
        removablePositions = new int[0];
        hoveredPosition = -1;
        System.out.println("Removal state cleared - ready for next move");
        repaint();
    }
    
    // Force complete state reset - for debugging and ensuring clean state
    public void forceStateReset() {
        awaitingRemoval = false;
        removablePositions = new int[0];
        hoveredPosition = -1;
        System.out.println("GameBoard state forcefully reset");
        repaint();
    }
}
