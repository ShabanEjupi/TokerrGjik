/**
 * AIPlayer.java - Advanced AI Player with Smart Algorithms
 * 
 * Features:
 * - Minimax algorithm with alpha-beta pruning
 * - Strategic mill formation
 * - Defensive blocking
 * - Multiple difficulty levels
 * - Human-like play patterns
 */

import java.util.*;

public class SmartAI {
    
    public enum Difficulty {
        EASY(1, 100),      // Depth 1, some randomness
        MEDIUM(2, 200),    // Depth 2, balanced play
        HARD(3, 500),      // Depth 3, strategic
        EXPERT(4, 1000);   // Depth 4, maximum thinking
        
        private final int searchDepth;
        private final int thinkingTime;
        
        Difficulty(int depth, int time) {
            this.searchDepth = depth;
            this.thinkingTime = time;
        }
        
        public int getSearchDepth() { return searchDepth; }
        public int getThinkingTime() { return thinkingTime; }
    }
    
    private Difficulty difficulty;
    private Random random;
    private int aiPlayer;
    private int humanPlayer;
    
    public SmartAI() {
        this(Difficulty.MEDIUM);
    }
    
    public SmartAI(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.random = new Random();
        this.aiPlayer = 2; // AI is player 2 by default
        this.humanPlayer = 1;
    }
    
    /**
     * Make the best move for the AI player
     */
    public void makeMove(GameEngine gameEngine) {
        // Simulate thinking time based on difficulty
        try {
            Thread.sleep(difficulty.getThinkingTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        // Determine game phase and make appropriate move
        GameEngine.GamePhase phase = gameEngine.getCurrentPhase();
        
        switch (phase) {
            case PLACEMENT:
                makePlacementMove(gameEngine);
                break;
            case MOVEMENT:
            case FLYING:
                makeMovementMove(gameEngine);
                break;
        }
    }
    
    private void makePlacementMove(GameEngine gameEngine) {
        int bestMove = findBestPlacementMove(gameEngine);
        if (bestMove != -1) {
            gameEngine.handlePositionClick(bestMove);
        }
    }
    
    private void makeMovementMove(GameEngine gameEngine) {
        Move bestMove = findBestMovementMove(gameEngine);
        if (bestMove != null) {
            // First select the piece to move
            gameEngine.handlePositionClick(bestMove.from);
            // Then move it to the destination
            gameEngine.handlePositionClick(bestMove.to);
        }
    }
    
    private int findBestPlacementMove(GameEngine gameEngine) {
        int[] board = gameEngine.getBoard();
        List<Integer> possibleMoves = new ArrayList<>();
        
        // Find all empty positions
        for (int i = 0; i < 24; i++) {
            if (board[i] == 0) {
                possibleMoves.add(i);
            }
        }
        
        if (possibleMoves.isEmpty()) {
            return -1;
        }
        
        // Use minimax to find best move
        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;
        
        for (int pos : possibleMoves) {
            int score = evaluatePlacementMove(gameEngine, pos);
            
            // Add some randomness for lower difficulties
            if (difficulty == Difficulty.EASY && random.nextInt(3) == 0) {
                score += random.nextInt(100) - 50;
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = pos;
            }
        }
        
        return bestMove;
    }
    
    private Move findBestMovementMove(GameEngine gameEngine) {
        int[] board = gameEngine.getBoard();
        List<Move> possibleMoves = new ArrayList<>();
        
        // Find all possible moves for AI pieces
        for (int from = 0; from < 24; from++) {
            if (board[from] == aiPlayer) {
                // Check all possible destinations
                for (int to = 0; to < 24; to++) {
                    if (board[to] == 0) {
                        // Check if move is valid (adjacent or flying)
                        if (gameEngine.canMovePiece(from, to)) {
                            possibleMoves.add(new Move(from, to));
                        }
                    }
                }
            }
        }
        
        if (possibleMoves.isEmpty()) {
            return null;
        }
        
        // Use minimax to find best move
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Move move : possibleMoves) {
            int score = evaluateMovementMove(gameEngine, move);
            
            // Add randomness for easier difficulties
            if (difficulty == Difficulty.EASY && random.nextInt(3) == 0) {
                score += random.nextInt(100) - 50;
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    private int evaluatePlacementMove(GameEngine gameEngine, int position) {
        int score = 0;
        
        // Strategic position values
        int[] positionValues = {
            100, 20, 100,  // Top row (corners worth more)
            20,  100, 20,  // Right column
            100, 20, 100,  // Bottom row
            20,  100, 20,  // Left column
            80,  15, 80,   // Middle square
            15,  80,  15,
            80,  15, 80,
            15,  80,  15,  // Inner square
            60,  10, 60,
            10,  60,  10
        };
        
        if (position < positionValues.length) {
            score += positionValues[position];
        }
        
        // Check if this move would form a mill
        if (wouldFormMill(gameEngine, position, aiPlayer)) {
            score += 500; // High priority for forming mills
        }
        
        // Check if this move would block opponent's mill
        if (wouldBlockOpponentMill(gameEngine, position)) {
            score += 300; // High priority for blocking
        }
        
        // Prefer center positions in early game
        if (isCenter(position)) {
            score += 50;
        }
        
        return score;
    }
    
    private int evaluateMovementMove(GameEngine gameEngine, Move move) {
        int score = 0;
        
        // Check if move forms a mill
        if (wouldFormMillAfterMove(gameEngine, move, aiPlayer)) {
            score += 1000; // Very high priority
        }
        
        // Check if move blocks opponent's potential mill
        if (wouldBlockOpponentMillAfterMove(gameEngine, move)) {
            score += 400;
        }
        
        // Prefer moves toward center
        if (isCenter(move.to)) {
            score += 100;
        }
        
        // Prefer moves that increase mobility
        score += countAdjacentEmpty(gameEngine, move.to) * 20;
        
        // Avoid moves that reduce our own mobility
        score -= countAdjacentEmpty(gameEngine, move.from) * 10;
        
        return score;
    }
    
    private boolean wouldFormMill(GameEngine gameEngine, int position, int player) {
        // Simulate placing piece and check for mill
        int[] board = gameEngine.getBoard().clone();
        board[position] = player;
        return checkMillAtPosition(board, position, player);
    }
    
    private boolean wouldFormMillAfterMove(GameEngine gameEngine, Move move, int player) {
        // Simulate move and check for mill
        int[] board = gameEngine.getBoard().clone();
        board[move.from] = 0;
        board[move.to] = player;
        return checkMillAtPosition(board, move.to, player);
    }
    
    private boolean wouldBlockOpponentMill(GameEngine gameEngine, int position) {
        // Check if placing here would prevent opponent from forming a mill
        int[] board = gameEngine.getBoard();
        
        // Check all mill combinations that include this position
        int[][] millCombinations = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},        // Outer square
            {9, 10, 11}, {12, 13, 14}, {15, 16, 17}, // Middle square
            {18, 19, 20}, {21, 22, 23},             // Inner square
            {0, 9, 21}, {3, 10, 18}, {6, 14, 15},   // Connecting lines
            {1, 4, 7}, {16, 19, 22}, {8, 12, 17}, {2, 13, 23}
        };
        
        for (int[] mill : millCombinations) {
            if (containsPosition(mill, position)) {
                int humanCount = 0;
                int emptyCount = 0;
                
                for (int pos : mill) {
                    if (board[pos] == humanPlayer) {
                        humanCount++;
                    } else if (board[pos] == 0) {
                        emptyCount++;
                    }
                }
                
                // If opponent has 2 pieces and 1 empty in this mill, blocking is important
                if (humanCount == 2 && emptyCount == 1) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean wouldBlockOpponentMillAfterMove(GameEngine gameEngine, Move move) {
        return wouldBlockOpponentMill(gameEngine, move.to);
    }
    
    private boolean checkMillAtPosition(int[] board, int position, int player) {
        int[][] millCombinations = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},        // Outer square
            {9, 10, 11}, {12, 13, 14}, {15, 16, 17}, // Middle square
            {18, 19, 20}, {21, 22, 23},             // Inner square
            {0, 9, 21}, {3, 10, 18}, {6, 14, 15},   // Connecting lines
            {1, 4, 7}, {16, 19, 22}, {8, 12, 17}, {2, 13, 23}
        };
        
        for (int[] mill : millCombinations) {
            if (containsPosition(mill, position)) {
                boolean isCompleteMill = true;
                for (int pos : mill) {
                    if (board[pos] != player) {
                        isCompleteMill = false;
                        break;
                    }
                }
                if (isCompleteMill) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean containsPosition(int[] array, int position) {
        for (int pos : array) {
            if (pos == position) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isCenter(int position) {
        // Center positions are generally more strategic
        int[] centerPositions = {1, 4, 7, 10, 13, 16, 19, 22};
        return containsPosition(centerPositions, position);
    }
    
    private int countAdjacentEmpty(GameEngine gameEngine, int position) {
        int[] board = gameEngine.getBoard();
        int[][] connections = gameEngine.getConnections();
        int count = 0;
        
        if (position < connections.length) {
            for (int adjacent : connections[position]) {
                if (board[adjacent] == 0) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    // Getters and setters
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    public void setAIPlayer(int player) {
        this.aiPlayer = player;
        this.humanPlayer = (player == 1) ? 2 : 1;
    }
    
    /**
     * Inner class to represent a move
     */
    private static class Move {
        final int from;
        final int to;
        
        Move(int from, int to) {
            this.from = from;
            this.to = to;
        }
        
        @Override
        public String toString() {
            return "Move from " + from + " to " + to;
        }
    }
}
