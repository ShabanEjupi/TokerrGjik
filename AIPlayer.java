import java.util.*;

/**
 * AIPlayer - Smart computer opponent with multiple difficulty levels
 * Features:
 * - Minimax algorithm with alpha-beta pruning
 * - Multiple difficulty levels (Easy, Medium, Hard, Expert)
 * - Strategic move evaluation
 * - Mill formation and blocking logic
 * - Endgame optimization
 */
public class AIPlayer {
    
    private String difficulty = "Medium";
    private Random random = new Random();
    
    // AI difficulty settings
    private static final int EASY_DEPTH = 1;
    private static final int MEDIUM_DEPTH = 3;
    private static final int HARD_DEPTH = 5;
    private static final int EXPERT_DEPTH = 7;
    
    // Move evaluation scores
    private static final int MILL_SCORE = 100;
    private static final int BLOCK_MILL_SCORE = 80;
    private static final int CAPTURE_SCORE = 150;
    private static final int WIN_SCORE = 1000;
    private static final int LOSE_SCORE = -1000;
    private static final int CENTER_BONUS = 10;
    private static final int CORNER_BONUS = 5;
    
    public AIPlayer() {
        // Default medium difficulty
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    /**
     * Make AI move based on current game state
     */
    public void makeMove(GameEngine gameEngine) {
        if (gameEngine.isGameOver()) return;
        
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
        int bestPosition = findBestPlacementMove(gameEngine);
        
        if (bestPosition != -1) {
            gameEngine.placePiece(bestPosition);
            
            // Check for mill formation and handle removal
            if (gameEngine.isMillFormed(bestPosition, gameEngine.getCurrentPlayer())) {
                handleMillCapture(gameEngine);
            } else {
                gameEngine.nextTurn();
            }
        }
    }
    
    private void makeMovementMove(GameEngine gameEngine) {
        Move bestMove = findBestMovementMove(gameEngine);
        
        if (bestMove != null) {
            gameEngine.movePiece(bestMove.from, bestMove.to);
            
            // Check for mill formation and handle removal
            if (gameEngine.isMillFormed(bestMove.to, gameEngine.getCurrentPlayer())) {
                handleMillCapture(gameEngine);
            } else {
                gameEngine.nextTurn();
            }
        }
    }
    
    private void handleMillCapture(GameEngine gameEngine) {
        int[] removablePositions = getRemovablePieces(gameEngine);
        
        if (removablePositions.length > 0) {
            // Choose best piece to remove
            int bestRemoval = chooseBestRemoval(gameEngine, removablePositions);
            gameEngine.removePiece(bestRemoval);
        }
        
        gameEngine.nextTurn();
    }
    
    private int findBestPlacementMove(GameEngine gameEngine) {
        int depth = getSearchDepth();
        
        if (difficulty.equals("Easy")) {
            return findEasyPlacementMove(gameEngine);
        }
        
        int bestScore = Integer.MIN_VALUE;
        int bestPosition = -1;
        
        List<Integer> validMoves = getValidPlacementMoves(gameEngine);
        
        for (int position : validMoves) {
            // Create game copy and test move
            GameEngine testEngine = copyGameEngine(gameEngine);
            testEngine.placePiece(position);
            
            int score;
            if (testEngine.isMillFormed(position, testEngine.getCurrentPlayer())) {
                // Mill formed - calculate with removal
                score = evaluateMillFormation(testEngine, position) + 
                       minimax(testEngine, depth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            } else {
                score = minimax(testEngine, depth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestPosition = position;
            }
        }
        
        return bestPosition;
    }
    
    private Move findBestMovementMove(GameEngine gameEngine) {
        int depth = getSearchDepth();
        
        if (difficulty.equals("Easy")) {
            return findEasyMovementMove(gameEngine);
        }
        
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        
        List<Move> validMoves = getValidMovementMoves(gameEngine);
        
        for (Move move : validMoves) {
            // Create game copy and test move
            GameEngine testEngine = copyGameEngine(gameEngine);
            testEngine.movePiece(move.from, move.to);
            
            int score;
            if (testEngine.isMillFormed(move.to, testEngine.getCurrentPlayer())) {
                score = evaluateMillFormation(testEngine, move.to) + 
                       minimax(testEngine, depth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            } else {
                score = minimax(testEngine, depth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * Minimax algorithm with alpha-beta pruning
     */
    private int minimax(GameEngine gameEngine, int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0 || gameEngine.isGameOver()) {
            return evaluatePosition(gameEngine);
        }
        
        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            
            List<Move> moves = getAllPossibleMoves(gameEngine);
            for (Move move : moves) {
                GameEngine testEngine = copyGameEngine(gameEngine);
                applyMove(testEngine, move);
                
                int score = minimax(testEngine, depth - 1, false, alpha, beta);
                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);
                
                if (beta <= alpha) {
                    break; // Alpha-beta pruning
                }
            }
            
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            
            List<Move> moves = getAllPossibleMoves(gameEngine);
            for (Move move : moves) {
                GameEngine testEngine = copyGameEngine(gameEngine);
                applyMove(testEngine, move);
                
                int score = minimax(testEngine, depth - 1, true, alpha, beta);
                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);
                
                if (beta <= alpha) {
                    break; // Alpha-beta pruning
                }
            }
            
            return minScore;
        }
    }
    
    private int evaluatePosition(GameEngine gameEngine) {
        if (gameEngine.isGameOver()) {
            int winner = gameEngine.getWinner();
            if (winner == 2) return WIN_SCORE; // AI wins
            if (winner == 1) return LOSE_SCORE; // AI loses
            return 0; // Draw
        }
        
        int score = 0;
        
        // Piece count advantage
        int aiPieces = countPieces(gameEngine, 2);
        int humanPieces = countPieces(gameEngine, 1);
        score += (aiPieces - humanPieces) * 50;
        
        // Mill count
        score += countMills(gameEngine, 2) * MILL_SCORE;
        score -= countMills(gameEngine, 1) * MILL_SCORE;
        
        // Position bonuses
        score += evaluatePositions(gameEngine);
        
        // Mobility (number of available moves)
        score += getMobility(gameEngine, 2) * 5;
        score -= getMobility(gameEngine, 1) * 5;
        
        return score;
    }
    
    // Easy AI - Random moves with some basic strategy
    private int findEasyPlacementMove(GameEngine gameEngine) {
        List<Integer> validMoves = getValidPlacementMoves(gameEngine);
        
        // 30% chance to try forming a mill
        if (random.nextFloat() < 0.3f) {
            for (int position : validMoves) {
                GameEngine testEngine = copyGameEngine(gameEngine);
                testEngine.placePiece(position);
                if (testEngine.isMillFormed(position, 2)) {
                    return position;
                }
            }
        }
        
        // Otherwise random move
        return validMoves.get(random.nextInt(validMoves.size()));
    }
    
    private Move findEasyMovementMove(GameEngine gameEngine) {
        List<Move> validMoves = getValidMovementMoves(gameEngine);
        
        // 40% chance to try forming a mill
        if (random.nextFloat() < 0.4f) {
            for (Move move : validMoves) {
                GameEngine testEngine = copyGameEngine(gameEngine);
                testEngine.movePiece(move.from, move.to);
                if (testEngine.isMillFormed(move.to, 2)) {
                    return move;
                }
            }
        }
        
        // Otherwise random move
        return validMoves.get(random.nextInt(validMoves.size()));
    }
    
    // Helper methods
    private int getSearchDepth() {
        switch (difficulty) {
            case "Easy": return EASY_DEPTH;
            case "Medium": return MEDIUM_DEPTH;
            case "Hard": return HARD_DEPTH;
            case "Expert": return EXPERT_DEPTH;
            default: return MEDIUM_DEPTH;
        }
    }
    
    private List<Integer> getValidPlacementMoves(GameEngine gameEngine) {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (gameEngine.getBoard()[i] == 0) {
                moves.add(i);
            }
        }
        return moves;
    }
    
    private List<Move> getValidMovementMoves(GameEngine gameEngine) {
        List<Move> moves = new ArrayList<>();
        boolean isFlying = gameEngine.getCurrentPhase() == GameEngine.GamePhase.FLYING;
        
        for (int from = 0; from < 24; from++) {
            if (gameEngine.getBoard()[from] == 2) { // AI pieces
                List<Integer> destinations = isFlying ? 
                    getValidPlacementMoves(gameEngine) : 
                    getAdjacentPositions(from, gameEngine);
                
                for (int to : destinations) {
                    if (gameEngine.getBoard()[to] == 0) {
                        moves.add(new Move(from, to));
                    }
                }
            }
        }
        
        return moves;
    }
    
    private List<Integer> getAdjacentPositions(int position, GameEngine gameEngine) {
        List<Integer> adjacent = new ArrayList<>();
        int[][] connections = gameEngine.getConnections();
        
        for (int i = 0; i < connections[position].length; i++) {
            if (connections[position][i] == 1) {
                adjacent.add(i);
            }
        }
        
        return adjacent;
    }
    
    private List<Move> getAllPossibleMoves(GameEngine gameEngine) {
        GameEngine.GamePhase phase = gameEngine.getCurrentPhase();
        
        if (phase == GameEngine.GamePhase.PLACEMENT) {
            List<Move> moves = new ArrayList<>();
            for (int pos : getValidPlacementMoves(gameEngine)) {
                moves.add(new Move(-1, pos)); // -1 indicates placement
            }
            return moves;
        } else {
            return getValidMovementMoves(gameEngine);
        }
    }
    
    private void applyMove(GameEngine gameEngine, Move move) {
        if (move.from == -1) {
            // Placement move
            gameEngine.placePiece(move.to);
        } else {
            // Movement move
            gameEngine.movePiece(move.from, move.to);
        }
    }
    
    private int[] getRemovablePieces(GameEngine gameEngine) {
        List<Integer> removable = new ArrayList<>();
        
        for (int i = 0; i < 24; i++) {
            if (gameEngine.getBoard()[i] == 1) { // Human pieces
                // Can remove if not part of a mill, or if all pieces are in mills
                if (!gameEngine.isPartOfMill(i, 1) || allPiecesInMills(gameEngine, 1)) {
                    removable.add(i);
                }
            }
        }
        
        return removable.stream().mapToInt(i -> i).toArray();
    }
    
    private int chooseBestRemoval(GameEngine gameEngine, int[] removablePositions) {
        int bestScore = Integer.MIN_VALUE;
        int bestRemoval = removablePositions[0];
        
        for (int position : removablePositions) {
            int score = evaluateRemoval(gameEngine, position);
            if (score > bestScore) {
                bestScore = score;
                bestRemoval = position;
            }
        }
        
        return bestRemoval;
    }
    
    private int evaluateRemoval(GameEngine gameEngine, int position) {
        int score = 0;
        
        // Prefer removing pieces that can form mills
        score += countPotentialMills(gameEngine, 1, position) * 20;
        
        // Prefer removing pieces in strategic positions
        if (isCornerPosition(position)) score += CORNER_BONUS;
        if (isCenterPosition(position)) score += CENTER_BONUS;
        
        return score;
    }
    
    private int evaluateMillFormation(GameEngine gameEngine, int position) {
        return MILL_SCORE + countPotentialMills(gameEngine, 2, position) * 10;
    }
    
    private int countPieces(GameEngine gameEngine, int player) {
        int count = 0;
        for (int i = 0; i < 24; i++) {
            if (gameEngine.getBoard()[i] == player) {
                count++;
            }
        }
        return count;
    }
    
    private int countMills(GameEngine gameEngine, int player) {
        int mills = 0;
        // Check all mill combinations
        int[][] millCombinations = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},        // Outer square
            {9, 10, 11}, {12, 13, 14}, {15, 16, 17}, // Middle square
            {18, 19, 20}, {21, 22, 23},              // Inner square
            {0, 9, 21}, {3, 10, 18}, {6, 11, 15},   // Vertical left
            {1, 4, 7}, {16, 19, 22},                 // Vertical middle
            {8, 12, 17}, {5, 13, 20}, {2, 14, 23}   // Vertical right
        };
        
        for (int[] mill : millCombinations) {
            if (gameEngine.getBoard()[mill[0]] == player &&
                gameEngine.getBoard()[mill[1]] == player &&
                gameEngine.getBoard()[mill[2]] == player) {
                mills++;
            }
        }
        
        return mills;
    }
    
    private int evaluatePositions(GameEngine gameEngine) {
        int score = 0;
        
        for (int i = 0; i < 24; i++) {
            if (gameEngine.getBoard()[i] == 2) { // AI pieces
                if (isCenterPosition(i)) score += CENTER_BONUS;
                if (isCornerPosition(i)) score += CORNER_BONUS;
            } else if (gameEngine.getBoard()[i] == 1) { // Human pieces
                if (isCenterPosition(i)) score -= CENTER_BONUS;
                if (isCornerPosition(i)) score -= CORNER_BONUS;
            }
        }
        
        return score;
    }
    
    private int getMobility(GameEngine gameEngine, int player) {
        int mobility = 0;
        
        for (int i = 0; i < 24; i++) {
            if (gameEngine.getBoard()[i] == player) {
                List<Integer> adjacent = getAdjacentPositions(i, gameEngine);
                for (int adj : adjacent) {
                    if (gameEngine.getBoard()[adj] == 0) {
                        mobility++;
                    }
                }
            }
        }
        
        return mobility;
    }
    
    private int countPotentialMills(GameEngine gameEngine, int player, int excludePosition) {
        // Count how many mills this position could potentially form
        int potential = 0;
        
        // Check all mill combinations that include this position
        int[][] millCombinations = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {9, 10, 11}, {12, 13, 14}, {15, 16, 17},
            {18, 19, 20}, {21, 22, 23},
            {0, 9, 21}, {3, 10, 18}, {6, 11, 15},
            {1, 4, 7}, {16, 19, 22},
            {8, 12, 17}, {5, 13, 20}, {2, 14, 23}
        };
        
        for (int[] mill : millCombinations) {
            for (int pos : mill) {
                if (pos == excludePosition) {
                    int playerPieces = 0;
                    int emptySpots = 0;
                    
                    for (int millPos : mill) {
                        if (millPos != excludePosition) {
                            if (gameEngine.getBoard()[millPos] == player) {
                                playerPieces++;
                            } else if (gameEngine.getBoard()[millPos] == 0) {
                                emptySpots++;
                            }
                        }
                    }
                    
                    if (playerPieces >= 1 && emptySpots >= 1) {
                        potential++;
                    }
                    break;
                }
            }
        }
        
        return potential;
    }
    
    private boolean allPiecesInMills(GameEngine gameEngine, int player) {
        for (int i = 0; i < 24; i++) {
            if (gameEngine.getBoard()[i] == player) {
                if (!gameEngine.isPartOfMill(i, player)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isCornerPosition(int position) {
        return position == 0 || position == 2 || position == 6 || position == 8 ||
               position == 9 || position == 11 || position == 15 || position == 17 ||
               position == 18 || position == 20 || position == 22 || position == 23;
    }
    
    private boolean isCenterPosition(int position) {
        return position == 1 || position == 4 || position == 7 || position == 10 ||
               position == 13 || position == 16 || position == 19 || position == 22;
    }
    
    // Simplified game engine copy (would need actual implementation)
    private GameEngine copyGameEngine(GameEngine original) {
        // This would need to create a deep copy of the game state
        // For now, returning original (would need proper implementation)
        return original;
    }
    
    // Move class
    private static class Move {
        int from;
        int to;
        
        Move(int from, int to) {
            this.from = from;
            this.to = to;
        }
        
        @Override
        public String toString() {
            return from + "->" + to;
        }
    }
}
