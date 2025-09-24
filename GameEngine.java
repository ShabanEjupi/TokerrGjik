/**
 * Game engine for Tokerr Gjik (Nine Men's Morris)
 * Handles game logic, rules, and state management with sound integration
 */
public class GameEngine {
    
    // Sound manager instance
    private SoundManager soundManager;
    
    // Game constants
    public static final int TOTAL_POSITIONS = 24;
    public static final int PIECES_PER_PLAYER = 9;
    public static final int MIN_PIECES_TO_FLY = 3;
    
    // Game phases
    public enum GamePhase {
        PLACEMENT,  // Vendosje - placing pieces
        MOVEMENT,   // Lëvizje - moving pieces
        FLYING      // Fluturim - flying with 3 or fewer pieces
    }
    
    // Player constants
    public static final int PLAYER_1 = 1;
    public static final int PLAYER_2 = 2;
    public static final int EMPTY = 0;
    
    // Game state
    private int[] board;              // 24 positions, 0=empty, 1=player1, 2=player2
    private int currentPlayer;        // Current player (1 or 2)
    private GamePhase currentPhase;   // Current game phase
    private int[] piecesRemaining;    // Pieces left to place [player1, player2]
    private int[] piecesOnBoard;      // Pieces currently on board [player1, player2]
    private int selectedPosition;     // Currently selected position for movement
    private boolean gameOver;         // Game over flag
    private int winner;               // Winner (1, 2, or 0 for draw)
    private boolean millFormed;       // Flag to track if current player formed a mill
    private boolean aiEnabled;        // AI opponent enabled
    private boolean isAiThinking;     // AI is making a move
    
    // Board connections - defines which positions are connected
    private static final int[][] CONNECTIONS = {
        // Position 0 connections
        {1, 9},
        // Position 1 connections  
        {0, 2, 4},
        // Position 2 connections
        {1, 14},
        // Position 3 connections
        {4, 10},
        // Position 4 connections
        {1, 3, 5, 7},
        // Position 5 connections
        {4, 13},
        // Position 6 connections
        {7, 11},
        // Position 7 connections
        {4, 6, 8},
        // Position 8 connections
        {7, 12},
        // Position 9 connections
        {0, 10, 21},
        // Position 10 connections
        {3, 9, 11, 18},
        // Position 11 connections
        {6, 10, 15},
        // Position 12 connections
        {8, 13, 17},
        // Position 13 connections
        {5, 12, 14, 20},
        // Position 14 connections
        {2, 13, 23},
        // Position 15 connections
        {11, 16},
        // Position 16 connections
        {15, 17, 19},
        // Position 17 connections
        {12, 16},
        // Position 18 connections
        {10, 19},
        // Position 19 connections
        {16, 18, 20, 22},
        // Position 20 connections
        {13, 19},
        // Position 21 connections
        {9, 22},
        // Position 22 connections
        {19, 21, 23},
        // Position 23 connections
        {14, 22}
    };
    
    // Mill combinations - all possible ways to form a mill (3 in a row)
    private static final int[][] MILLS = {
        {0, 1, 2},    // Top horizontal
        {3, 4, 5},    // Middle top horizontal
        {6, 7, 8},    // Middle bottom horizontal
        {9, 10, 11},  // Left middle horizontal
        {12, 13, 14}, // Right middle horizontal
        {15, 16, 17}, // Bottom top horizontal
        {18, 19, 20}, // Bottom middle horizontal
        {21, 22, 23}, // Bottom horizontal
        {0, 9, 21},   // Left vertical
        {3, 10, 18},  // Left middle vertical
        {6, 11, 15},  // Left inner vertical
        {1, 4, 7},    // Top vertical
        {16, 19, 22}, // Bottom vertical
        {8, 12, 17},  // Right inner vertical
        {5, 13, 20},  // Right middle vertical
        {2, 14, 23}   // Right vertical
    };
    
    public GameEngine() {
        soundManager = SoundManager.getInstance();
        initializeGame();
    }
    
    public void initializeGame() {
        board = new int[TOTAL_POSITIONS];
        currentPlayer = PLAYER_1;
        currentPhase = GamePhase.PLACEMENT;
        piecesRemaining = new int[]{PIECES_PER_PLAYER, PIECES_PER_PLAYER};
        piecesOnBoard = new int[]{0, 0};
        selectedPosition = -1;
        gameOver = false;
        winner = 0;
        millFormed = false;
        
        // Initialize board to empty
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            board[i] = EMPTY;
        }
        
        // Play game start sound
        soundManager.playSound(SoundManager.GAME_START);
    }
    
    /**
     * Handle a position click/touch
     * @param position The position that was clicked (0-23)
     * @return true if the move was valid and executed
     */
    public boolean handlePositionClick(int position) {
        if (gameOver || position < 0 || position >= TOTAL_POSITIONS) {
            return false;
        }
        
        switch (currentPhase) {
            case PLACEMENT:
                return handlePlacement(position);
            case MOVEMENT:
            case FLYING:
                return handleMovement(position);
        }
        
        return false;
    }
    
    private boolean handlePlacement(int position) {
        // Can only place on empty positions
        if (board[position] != EMPTY) {
            return false;
        }
        
        // Check if current player has pieces left to place
        if (piecesRemaining[currentPlayer - 1] <= 0) {
            return false; // No more pieces to place
        }
        
        // Place the piece
        board[position] = currentPlayer;
        piecesRemaining[currentPlayer - 1]--;
        piecesOnBoard[currentPlayer - 1]++;
        
        // Play piece placement sound
        soundManager.playSound(SoundManager.PIECE_PLACE);
        
        System.out.println("Player " + currentPlayer + " placed piece at position " + position);
        System.out.println("Pieces remaining: P1=" + piecesRemaining[0] + ", P2=" + piecesRemaining[1]);
        System.out.println("Pieces on board: P1=" + piecesOnBoard[0] + ", P2=" + piecesOnBoard[1]);
        
        // Check for mill formation
        millFormed = isMillFormed(position, currentPlayer);
        
        if (millFormed) {
            // Play mill formation sound
            soundManager.playSound(SoundManager.MILL_FORMED);
        }
        
        if (!millFormed) {
            // No mill formed, switch to next player
            switchPlayer();
            
            // Check if placement phase is over
            if (piecesRemaining[0] == 0 && piecesRemaining[1] == 0) {
                currentPhase = GamePhase.MOVEMENT;
                System.out.println("Placement phase over, switching to MOVEMENT phase");
            }
        }
        // If mill was formed, don't switch players yet - wait for piece removal
        
        return true;
    }
    
    private boolean handleMovement(int position) {
        if (selectedPosition == -1) {
            // Selecting a piece to move
            if (board[position] == currentPlayer) {
                selectedPosition = position;
                return true;
            }
            return false;
        } else {
            // Moving the selected piece
            if (position == selectedPosition) {
                // Deselect
                selectedPosition = -1;
                return true;
            }
            
            if (board[position] != EMPTY) {
                // Can't move to occupied position
                return false;
            }
            
            // Check if move is valid (connected positions or flying)
            boolean canMove = false;
            if (currentPhase == GamePhase.FLYING && piecesOnBoard[currentPlayer - 1] <= MIN_PIECES_TO_FLY) {
                // Can fly to any empty position
                canMove = true;
            } else {
                // Must move to connected position
                canMove = arePositionsConnected(selectedPosition, position);
            }
            
            if (canMove) {
                // Execute the move
                board[position] = currentPlayer;
                board[selectedPosition] = EMPTY;
                selectedPosition = -1;
                
                // Check for mill formation
                millFormed = isMillFormed(position, currentPlayer);
                
                if (!millFormed) {
                    // No mill formed, switch to next player
                    switchPlayer();
                    
                    // Update phase if necessary
                    updateGamePhase();
                }
                // If mill was formed, don't switch players yet - wait for piece removal
                
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Remove an opponent piece (called after forming a mill)
     * @param position Position of the piece to remove (-1 to skip removal)
     * @return true if removal was successful or skipped
     */
    public boolean removePiece(int position) {
        System.out.println("=== REMOVE PIECE DEBUG ===");
        System.out.println("Attempting to remove piece at position: " + position);
        System.out.println("Current player before removal: " + currentPlayer);
        System.out.println("Mill formed flag before removal: " + millFormed);
        
        // If position is -1, skip removal (all opponent pieces are in mills)
        if (position == -1) {
            System.out.println("Skipping removal - all opponent pieces are in mills");
            millFormed = false;
            switchPlayer();
            updateGamePhase();
            System.out.println("After skip: Current player = " + currentPlayer + ", Mill formed = " + millFormed);
            return true;
        }
        
        int opponent = (currentPlayer == PLAYER_1) ? PLAYER_2 : PLAYER_1;
        
        if (position < 0 || position >= TOTAL_POSITIONS) {
            System.out.println("Invalid position: " + position);
            return false;
        }
        
        if (board[position] != opponent) {
            System.out.println("Position " + position + " does not contain opponent piece. Contains: " + board[position] + ", opponent is: " + opponent);
            return false;
        }
        
        // Check if the piece is part of a mill (can't remove if it is, unless all pieces are in mills)
        if (isPartOfMill(position, opponent)) {
            // Only allow removal if ALL opponent pieces are in mills
            if (hasNonMillPieces(opponent)) {
                System.out.println("Cannot remove piece at position " + position + " - it's part of a mill and opponent has non-mill pieces");
                return false;
            }
        }
        
        // Remove the piece
        System.out.println("Removing opponent piece at position " + position);
        board[position] = EMPTY;
        piecesOnBoard[opponent - 1]--;
        
        // CRITICAL: Clear mill formed flag FIRST before any other operations
        millFormed = false;
        System.out.println("Mill formed flag cleared: " + millFormed);
        
        // Check win condition
        checkWinCondition();
        
        // Switch player only if game is not over
        if (!gameOver) {
            switchPlayer();
            updateGamePhase();
            System.out.println("After removal: Current player = " + currentPlayer + ", Phase = " + currentPhase + ", Mill formed = " + millFormed);
        } else {
            System.out.println("Game is over, winner: " + winner);
        }
        
        System.out.println("Piece removed successfully. Opponent now has " + piecesOnBoard[opponent - 1] + " pieces");
        System.out.println("=== END REMOVE PIECE DEBUG ===");
        return true;
    }
    
    // Additional methods for enhanced features
    public void startNewGame() {
        initializeGame();
    }
    
    public void setAIEnabled(boolean enabled) {
        this.aiEnabled = enabled;
    }
    
    public boolean isAIEnabled() {
        return aiEnabled;
    }
    
    public void nextTurn() {
        switchPlayer();
    }
    
    public int getPieceAt(int position) {
        if (position >= 0 && position < TOTAL_POSITIONS) {
            return board[position];
        }
        return EMPTY;
    }
    
    public boolean canPlacePiece(int position) {
        return position >= 0 && position < TOTAL_POSITIONS && 
               board[position] == EMPTY && 
               currentPhase == GamePhase.PLACEMENT &&
               piecesRemaining[currentPlayer - 1] > 0;
    }
    
    public void placePiece(int position) {
        if (canPlacePiece(position)) {
            board[position] = currentPlayer;
            piecesRemaining[currentPlayer - 1]--;
            piecesOnBoard[currentPlayer - 1]++;
            
            // Play sound
            if (soundManager != null) {
                soundManager.playSound(SoundManager.PIECE_PLACE);
            }
            
            // Check phase transition
            if (piecesRemaining[0] == 0 && piecesRemaining[1] == 0) {
                currentPhase = GamePhase.MOVEMENT;
            }
        }
    }
    
    public boolean canSelectPiece(int position) {
        return position >= 0 && position < TOTAL_POSITIONS && 
               board[position] == currentPlayer &&
               (currentPhase == GamePhase.MOVEMENT || currentPhase == GamePhase.FLYING);
    }
    
    public boolean canMovePiece(int from, int to) {
        if (from < 0 || from >= TOTAL_POSITIONS || 
            to < 0 || to >= TOTAL_POSITIONS) {
            return false;
        }
        
        if (board[from] != currentPlayer || board[to] != EMPTY) {
            return false;
        }
        
        if (currentPhase == GamePhase.FLYING) {
            return true; // Can move to any empty position when flying
        }
        
        // Check if positions are connected
        for (int connected : CONNECTIONS[from]) {
            if (connected == to) {
                return true;
            }
        }
        return false;
    }
    
    public void movePiece(int from, int to) {
        if (canMovePiece(from, to)) {
            board[from] = EMPTY;
            board[to] = currentPlayer;
            
            // Play sound
            if (soundManager != null) {
                soundManager.playSound(SoundManager.PIECE_MOVE);
            }
            
            // Check if player should enter flying phase
            if (piecesOnBoard[currentPlayer - 1] <= MIN_PIECES_TO_FLY) {
                currentPhase = GamePhase.FLYING;
            }
        }
    }
    
    public int[] getRemovablePieces() {
        java.util.List<Integer> removable = new java.util.ArrayList<>();
        int opponent = (currentPlayer == PLAYER_1) ? PLAYER_2 : PLAYER_1;
        
        // Find all opponent pieces that are not in mills
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            if (board[i] == opponent && !isPartOfMill(i, opponent)) {
                removable.add(i);
            }
        }
        
        // If all opponent pieces are in mills, any can be removed
        if (removable.isEmpty()) {
            for (int i = 0; i < TOTAL_POSITIONS; i++) {
                if (board[i] == opponent) {
                    removable.add(i);
                }
            }
        }
        
        return removable.stream().mapToInt(Integer::intValue).toArray();
    }
    
    // Remove duplicate method - keep existing private implementation
    
    public int[][] getConnections() {
        // Convert connections array to 2D matrix format
        int[][] connectionMatrix = new int[TOTAL_POSITIONS][TOTAL_POSITIONS];
        
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            for (int j = 0; j < CONNECTIONS[i].length; j++) {
                int connected = CONNECTIONS[i][j];
                connectionMatrix[i][connected] = 1;
                connectionMatrix[connected][i] = 1; // Bidirectional
            }
        }
        
        return connectionMatrix;
    }
    
    private void switchPlayer() {
        currentPlayer = (currentPlayer == PLAYER_1) ? PLAYER_2 : PLAYER_1;
        
        // Play turn change sound
        soundManager.playSound(SoundManager.TURN_CHANGE);
    }
    
    private void updateGamePhase() {
        // First check if all placement pieces are used
        if (piecesRemaining[0] == 0 && piecesRemaining[1] == 0) {
            // All pieces placed, now check if any player should be in flying phase
            if (piecesOnBoard[currentPlayer - 1] <= MIN_PIECES_TO_FLY) {
                currentPhase = GamePhase.FLYING;
            } else {
                currentPhase = GamePhase.MOVEMENT;
            }
        }
        // If still in placement phase, stay there even if a player has few pieces on board
        // Players can only enter flying phase after all pieces are placed
    }
    
    boolean isMillFormed(int position, int player) {
        for (int[] mill : MILLS) {
            if (contains(mill, position)) {
                // Check if all positions in this mill belong to the player
                boolean millFormed = true;
                for (int pos : mill) {
                    if (board[pos] != player) {
                        millFormed = false;
                        break;
                    }
                }
                if (millFormed) {
                    return true;
                }
            }
        }
        return false;
    }
    
    boolean isPartOfMill(int position, int player) {
        for (int[] mill : MILLS) {
            if (contains(mill, position)) {
                boolean allSame = true;
                for (int pos : mill) {
                    if (board[pos] != player) {
                        allSame = false;
                        break;
                    }
                }
                if (allSame) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean hasNonMillPieces(int player) {
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            if (board[i] == player && !isPartOfMill(i, player)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean arePositionsConnected(int pos1, int pos2) {
        return contains(CONNECTIONS[pos1], pos2);
    }
    
    private boolean contains(int[] array, int value) {
        for (int item : array) {
            if (item == value) {
                return true;
            }
        }
        return false;
    }
    
    private void checkWinCondition() {
        // Win if opponent has less than 3 pieces
        int opponent = (currentPlayer == PLAYER_1) ? PLAYER_2 : PLAYER_1;
        if (piecesOnBoard[opponent - 1] < 3 && piecesRemaining[opponent - 1] == 0) {
            gameOver = true;
            winner = currentPlayer;
            return;
        }
        
        // Win if opponent has no valid moves (only check in movement phase)
        if (currentPhase != GamePhase.PLACEMENT && !hasValidMoves(opponent)) {
            gameOver = true;
            winner = currentPlayer;
        }
    }
    
    private boolean hasValidMoves(int player) {
        // If player can fly, they always have moves if there are empty positions
        if (piecesOnBoard[player - 1] <= MIN_PIECES_TO_FLY) {
            for (int i = 0; i < TOTAL_POSITIONS; i++) {
                if (board[i] == EMPTY) {
                    return true;
                }
            }
            return false;
        }
        
        // Check if any piece can move to a connected empty position
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            if (board[i] == player) {
                for (int connected : CONNECTIONS[i]) {
                    if (board[connected] == EMPTY) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    // Getters
    public int[] getBoard() { return board.clone(); }
    public int getCurrentPlayer() { return currentPlayer; }
    public GamePhase getCurrentPhase() { return currentPhase; }
    public int[] getPiecesRemaining() { return piecesRemaining.clone(); }
    public int[] getPiecesOnBoard() { return piecesOnBoard.clone(); }
    public int getSelectedPosition() { return selectedPosition; }
    public boolean isGameOver() { return gameOver; }
    public int getWinner() { return winner; }
    public boolean wasMillFormed() { return millFormed; }
    
    /**
     * Force clear the mill formed state - used for debugging and ensuring clean state
     */
    public void clearMillFormed() {
        System.out.println("Force clearing mill formed flag");
        millFormed = false;
    }
    
    /**
     * Get detailed game state for debugging
     */
    public String getGameStateDebug() {
        return String.format("Player: %d, Phase: %s, Mill: %s, Selected: %d, GameOver: %s", 
                           currentPlayer, currentPhase, millFormed, selectedPosition, gameOver);
    }
    
    /**
     * Get available positions for piece removal (after forming a mill)
     */
    public int[] getRemovablePositions() {
        int opponent = (currentPlayer == PLAYER_1) ? PLAYER_2 : PLAYER_1;
        java.util.ArrayList<Integer> removable = new java.util.ArrayList<>();
        
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            if (board[i] == opponent) {
                if (!isPartOfMill(i, opponent) || !hasNonMillPieces(opponent)) {
                    removable.add(i);
                }
            }
        }
        
        return removable.stream().mapToInt(i -> i).toArray();
    }
    
    // AI Methods
    
    /**
     * Enable/disable AI opponent for Player 2
     */
    public void setAiEnabled(boolean enabled) {
        this.aiEnabled = enabled;
    }
    
    public boolean isAiEnabled() {
        return aiEnabled;
    }
    
    public boolean isAiPlayer() {
        return aiEnabled && currentPlayer == PLAYER_2;
    }
    
    public boolean isAiThinking() {
        return isAiThinking;
    }
    
    /**
     * Make AI move for current player (if AI is enabled and it's Player 2's turn)
     */
    public boolean makeAiMove() {
        if (!isAiPlayer() || gameOver) {
            return false;
        }
        
        isAiThinking = true;
        
        try {
            // Small delay to simulate thinking
            Thread.sleep(500);
            
            boolean moveMade = false;
            
            switch (currentPhase) {
                case PLACEMENT:
                    moveMade = makeAiPlacementMove();
                    break;
                case MOVEMENT:
                    moveMade = makeAiMovementMove();
                    break;
                case FLYING:
                    moveMade = makeAiFlyingMove();
                    break;
            }
            
            // If AI formed a mill, it needs to remove an opponent piece
            if (moveMade && millFormed) {
                int[] removablePositions = getRemovablePositions();
                if (removablePositions.length > 0) {
                    // Choose best piece to remove (prioritize non-mill pieces)
                    int bestRemoval = chooseBestRemoval(removablePositions);
                    removePiece(bestRemoval);
                }
            }
            
            return moveMade;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            isAiThinking = false;
        }
    }
    
    private boolean makeAiPlacementMove() {
        // Strategy: Try to form mills, block opponent mills, or place strategically
        
        // 1. Check if AI can complete a mill
        for (int pos = 0; pos < TOTAL_POSITIONS; pos++) {
            if (board[pos] == EMPTY && wouldFormMill(pos, PLAYER_2)) {
                return handlePositionClick(pos);
            }
        }
        
        // 2. Block opponent from forming mills
        for (int pos = 0; pos < TOTAL_POSITIONS; pos++) {
            if (board[pos] == EMPTY && wouldFormMill(pos, PLAYER_1)) {
                return handlePositionClick(pos);
            }
        }
        
        // 3. Place in strategic positions (corners and intersections)
        int[] strategicPositions = {0, 2, 6, 8, 18, 20, 21, 23, 9, 11, 12, 14};
        for (int pos : strategicPositions) {
            if (board[pos] == EMPTY) {
                return handlePositionClick(pos);
            }
        }
        
        // 4. Place anywhere available
        for (int pos = 0; pos < TOTAL_POSITIONS; pos++) {
            if (board[pos] == EMPTY) {
                return handlePositionClick(pos);
            }
        }
        
        return false;
    }
    
    private boolean makeAiMovementMove() {
        // Find AI pieces and try to move them strategically
        for (int from = 0; from < TOTAL_POSITIONS; from++) {
            if (board[from] == PLAYER_2) {
                // Try to move this piece to form a mill or block opponent
                for (int to : getConnectedPositions(from)) {
                    if (board[to] == EMPTY) {
                        // Simulate the move
                        board[from] = EMPTY;
                        board[to] = PLAYER_2;
                        
                        boolean formsMill = isMillFormed(to, PLAYER_2);
                        boolean blocksMill = wouldBlockOpponentMill(from, to);
                        
                        // Undo simulation
                        board[from] = PLAYER_2;
                        board[to] = EMPTY;
                        
                        if (formsMill || blocksMill) {
                            selectedPosition = from;
                            return handlePositionClick(to);
                        }
                    }
                }
            }
        }
        
        // If no strategic move, make any valid move
        for (int from = 0; from < TOTAL_POSITIONS; from++) {
            if (board[from] == PLAYER_2) {
                for (int to : getConnectedPositions(from)) {
                    if (board[to] == EMPTY) {
                        selectedPosition = from;
                        return handlePositionClick(to);
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean makeAiFlyingMove() {
        // In flying phase, AI can move to any empty position
        for (int from = 0; from < TOTAL_POSITIONS; from++) {
            if (board[from] == PLAYER_2) {
                // Try to move to form a mill
                for (int to = 0; to < TOTAL_POSITIONS; to++) {
                    if (board[to] == EMPTY) {
                        // Simulate the move
                        board[from] = EMPTY;
                        board[to] = PLAYER_2;
                        
                        boolean formsMill = isMillFormed(to, PLAYER_2);
                        
                        // Undo simulation
                        board[from] = PLAYER_2;
                        board[to] = EMPTY;
                        
                        if (formsMill) {
                            selectedPosition = from;
                            return handlePositionClick(to);
                        }
                    }
                }
            }
        }
        
        // If no mill-forming move, make any valid move
        for (int from = 0; from < TOTAL_POSITIONS; from++) {
            if (board[from] == PLAYER_2) {
                for (int to = 0; to < TOTAL_POSITIONS; to++) {
                    if (board[to] == EMPTY) {
                        selectedPosition = from;
                        return handlePositionClick(to);
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean wouldFormMill(int position, int player) {
        if (board[position] != EMPTY) return false;
        
        board[position] = player; // Simulate placement
        boolean formsMill = isMillFormed(position, player);
        board[position] = EMPTY; // Undo simulation
        
        return formsMill;
    }
    
    private boolean wouldBlockOpponentMill(int from, int to) {
        int opponent = PLAYER_1;
        
        // Check if moving from 'from' to 'to' would prevent opponent from forming a mill
        board[from] = EMPTY;
        board[to] = PLAYER_2;
        
        boolean blocksAnyMill = false;
        for (int pos = 0; pos < TOTAL_POSITIONS; pos++) {
            if (board[pos] == EMPTY && wouldFormMill(pos, opponent)) {
                blocksAnyMill = true;
                break;
            }
        }
        
        // Undo simulation
        board[from] = PLAYER_2;
        board[to] = EMPTY;
        
        return blocksAnyMill;
    }
    
    private int[] getConnectedPositions(int position) {
        return CONNECTIONS[position];
    }
    
    private int chooseBestRemoval(int[] removablePositions) {
        // Prioritize removing pieces that are not part of potential mills
        for (int pos : removablePositions) {
            // Remove pieces that would prevent opponent from forming mills
            if (!isPartOfMill(pos, PLAYER_1)) {
                return pos;
            }
        }
        
        // If all are part of mills, remove any
        return removablePositions[0];
    }

    // End of GameEngine class
}
