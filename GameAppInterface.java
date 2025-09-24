/**
 * GameAppInterface - Interface for game applications
 * Allows GameBoard to work with different UI implementations
 */
public interface GameAppInterface {
    /**
     * Called when the game state changes and UI needs to be refreshed
     */
    void updateGameStatus();
    
    /**
     * Award points for capturing a piece
     * @param player The player who captured (1 or 2)
     */
    default void awardCapturePoints(int player) {
        // Default empty implementation
    }
    
    /**
     * Award points for forming a mill
     * @param player The player who formed the mill (1 or 2)
     */
    default void awardMillPoints(int player) {
        // Default empty implementation
    }
}
