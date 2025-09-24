/**
 * SoundManager.java - Modern Sound System for TokerGjik
 * Hand    // File loading removed - all sounds generated programmatically for mobile experienceounds including piece placement, mill formation, and ambient music
 * Cross-platform compatible using Java's built-in sound capabilities
 */

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {
    
    private static SoundManager instance;
    private final Map<String, Clip> soundClips = new HashMap<>();
    private final ExecutorService soundExecutor = Executors.newCachedThreadPool();
    private boolean soundEnabled = true;
    private float masterVolume = 0.7f;
    
    // Sound effect constants
    public static final String PIECE_PLACE = "piece_place";
    public static final String PIECE_MOVE = "piece_move";
    public static final String MILL_FORMED = "mill_formed";
    public static final String PIECE_CAPTURE = "piece_capture";
    public static final String GAME_WIN = "game_win";
    public static final String GAME_LOSE = "game_lose";
    public static final String BUTTON_HOVER = "button_hover";
    public static final String BUTTON_CLICK = "button_click";
    public static final String GAME_START = "game_start";
    public static final String TURN_CHANGE = "turn_change";
    public static final String ERROR_SOUND = "error";
    // Background music constants and variables removed as requested
    
    private SoundManager() {
        initializeSounds();
    }
    
    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    private void initializeSounds() {
        try {
            // Generate mobile-style sounds programmatically - no file loading needed
            createMobilePiecePlaceSound();
            createMobilePieceMoveSound();
            createMobileMillFormedSound();
            createMobilePieceCaptureSound();
            createMobileButtonClickSound();
            createMobileButtonHoverSound();
            createMobileGameWinSound();
            createMobileGameLoseSound();
            createMobileGameStartSound();
            createMobileTurnChangeSound();
            createMobileErrorSound();
            
            // No background music - removed as requested
            
            System.out.println("🔊 SoundManager initialized with " + soundClips.size() + " mobile-style sound effects");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing sounds: " + e.getMessage());
        }
    }
    
    // File loading method removed - using mobile-style programmatic sounds only
    
    // Play a sound effect
    public void playSound(String soundName) {
        if (!soundEnabled) return;
        
        soundExecutor.submit(() -> {
            try {
                Clip clip = soundClips.get(soundName);
                if (clip != null) {
                    clip.setFramePosition(0);
                    setVolume(clip, masterVolume);
                    clip.start();
                }
            } catch (Exception e) {
                System.err.println("Error playing sound " + soundName + ": " + e.getMessage());
            }
        });
    }
    
    // Background music removed as requested by user
    
    // Set master volume (0.0 to 1.0)
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    // Music volume control removed with background music
    
    // Enable/disable sound effects
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    // Background music functionality removed as requested
    
    // Set volume for a specific clip
    private void setVolume(Clip clip, float volume) {
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(dB, gainControl.getMaximum())));
        } catch (Exception e) {
            // Volume control not supported
        }
    }
    
    // Create mobile-style synthesized sounds (iOS/Android inspired)
    private void createMobilePiecePlaceSound() throws Exception {
        // Soft tap sound - like iOS keyboard tap
        byte[] buffer = generateMobileClickSound(1200, 0.08, 0.4);
        soundClips.put(PIECE_PLACE, createClipFromBuffer(buffer));
    }
    
    private void createMobilePieceMoveSound() throws Exception {
        // Subtle swipe sound - like iOS navigation
        byte[] buffer = generateMobileSwipeSound(800, 0.12, 0.3);
        soundClips.put(PIECE_MOVE, createClipFromBuffer(buffer));
    }
    
    private void createMobileMillFormedSound() throws Exception {
        // Success chime - like iOS notification
        byte[] buffer = generateMobileSuccessChime();
        soundClips.put(MILL_FORMED, createClipFromBuffer(buffer));
    }
    
    private void createMobilePieceCaptureSound() throws Exception {
        // Pop sound - like iOS delete/remove
        byte[] buffer = generateMobilePopSound(600, 0.15, 0.5);
        soundClips.put(PIECE_CAPTURE, createClipFromBuffer(buffer));
    }
    
    private void createMobileButtonClickSound() throws Exception {
        // Crisp button tap - like iOS button press
        byte[] buffer = generateMobileClickSound(1000, 0.06, 0.6);
        soundClips.put(BUTTON_CLICK, createClipFromBuffer(buffer));
    }
    
    private void createMobileButtonHoverSound() throws Exception {
        // Subtle hover sound - like iOS selection
        byte[] buffer = generateMobileClickSound(1400, 0.04, 0.2);
        soundClips.put(BUTTON_HOVER, createClipFromBuffer(buffer));
    }
    
    private void createMobileGameWinSound() throws Exception {
        // Victory fanfare - like iOS achievement
        byte[] buffer = generateMobileVictoryFanfare();
        soundClips.put(GAME_WIN, createClipFromBuffer(buffer));
    }
    
    private void createMobileGameLoseSound() throws Exception {
        // Gentle fail sound - like iOS error but softer
        byte[] buffer = generateMobileFailSound();
        soundClips.put(GAME_LOSE, createClipFromBuffer(buffer));
    }
    
    private void createMobileGameStartSound() throws Exception {
        // Game begin chime - like iOS app launch
        byte[] buffer = generateMobileStartChime();
        soundClips.put(GAME_START, createClipFromBuffer(buffer));
    }
    
    private void createMobileTurnChangeSound() throws Exception {
        // Turn change ding - like iOS message sent
        byte[] buffer = generateMobileClickSound(880, 0.1, 0.4);
        soundClips.put(TURN_CHANGE, createClipFromBuffer(buffer));
    }
    
    private void createMobileErrorSound() throws Exception {
        // Error beep - like iOS system alert
        byte[] buffer = generateMobileErrorBeep();
        soundClips.put(ERROR_SOUND, createClipFromBuffer(buffer));
    }
    
    // Old tone generation methods removed - replaced with mobile-style sounds above
    
    // Generate mobile-style click sound (crisp and short like iOS tap)
    private byte[] generateMobileClickSound(double frequency, double duration, double amplitude) {
        int sampleRate = 44100;
        int numSamples = (int) (duration * sampleRate);
        byte[] buffer = new byte[numSamples * 2];
        
        for (int i = 0; i < numSamples; i++) {
            // Create a sharp attack with quick decay - typical mobile tap sound
            double t = (double) i / sampleRate;
            double envelope = Math.exp(-t * 15); // Quick exponential decay
            
            // Mix fundamental with harmonics for richer mobile sound
            double sample = amplitude * envelope * (
                Math.sin(2 * Math.PI * frequency * t) * 0.7 +
                Math.sin(2 * Math.PI * frequency * 2 * t) * 0.2 +
                Math.sin(2 * Math.PI * frequency * 3 * t) * 0.1
            );
            
            short shortSample = (short) (sample * Short.MAX_VALUE);
            buffer[i * 2] = (byte) (shortSample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((shortSample >> 8) & 0xFF);
        }
        
        return buffer;
    }
    
    // Generate mobile swipe sound (subtle frequency sweep)
    private byte[] generateMobileSwipeSound(double startFreq, double duration, double amplitude) {
        int sampleRate = 44100;
        int numSamples = (int) (duration * sampleRate);
        byte[] buffer = new byte[numSamples * 2];
        
        double endFreq = startFreq * 0.7; // Frequency sweep down
        
        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / sampleRate;
            double progress = t / duration;
            
            // Linear frequency sweep with bell-shaped envelope
            double freq = startFreq + (endFreq - startFreq) * progress;
            double envelope = amplitude * Math.exp(-0.5 * Math.pow((progress - 0.5) / 0.3, 2)); // Gaussian envelope
            
            double sample = envelope * Math.sin(2 * Math.PI * freq * t);
            
            short shortSample = (short) (sample * Short.MAX_VALUE);
            buffer[i * 2] = (byte) (shortSample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((shortSample >> 8) & 0xFF);
        }
        
        return buffer;
    }
    
    // Generate mobile success chime (iOS-style notification)
    private byte[] generateMobileSuccessChime() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Two-tone chime: C -> E
        byte[] note1 = generateMobileClickSound(523.25, 0.15, 0.6); // C5
        byte[] note2 = generateMobileClickSound(659.25, 0.2, 0.5);  // E5
        
        baos.write(note1, 0, note1.length);
        baos.write(note2, 0, note2.length);
        
        return baos.toByteArray();
    }
    
    // Generate mobile pop sound (like bubble pop or delete)
    private byte[] generateMobilePopSound(double frequency, double duration, double amplitude) {
        int sampleRate = 44100;
        int numSamples = (int) (duration * sampleRate);
        byte[] buffer = new byte[numSamples * 2];
        
        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / sampleRate;
            double progress = t / duration;
            
            // Bubble-like frequency modulation
            double freqMod = frequency * (1 + 0.3 * Math.sin(2 * Math.PI * 8 * t));
            double envelope = amplitude * Math.exp(-progress * 8) * (1 - progress);
            
            double sample = envelope * Math.sin(2 * Math.PI * freqMod * t);
            
            short shortSample = (short) (sample * Short.MAX_VALUE);
            buffer[i * 2] = (byte) (shortSample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((shortSample >> 8) & 0xFF);
        }
        
        return buffer;
    }
    
    // Generate mobile victory fanfare (ascending chime)
    private byte[] generateMobileVictoryFanfare() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Ascending victory melody: C -> E -> G -> C
        double[] frequencies = {523.25, 659.25, 783.99, 1046.50};
        double[] durations = {0.12, 0.12, 0.12, 0.25};
        
        for (int i = 0; i < frequencies.length; i++) {
            byte[] note = generateMobileClickSound(frequencies[i], durations[i], 0.6 - i * 0.1);
            baos.write(note, 0, note.length);
        }
        
        return baos.toByteArray();
    }
    
    // Generate mobile fail sound (gentle descending tone)
    private byte[] generateMobileFailSound() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Gentle descending melody: G -> E -> C
        byte[] note1 = generateMobileClickSound(783.99, 0.15, 0.4); // G5
        byte[] note2 = generateMobileClickSound(659.25, 0.15, 0.4); // E5
        byte[] note3 = generateMobileClickSound(523.25, 0.2, 0.4);  // C5
        
        baos.write(note1, 0, note1.length);
        baos.write(note2, 0, note2.length);
        baos.write(note3, 0, note3.length);
        
        return baos.toByteArray();
    }
    
    // Generate mobile start chime (welcoming sound)
    private byte[] generateMobileStartChime() {
        return generateMobileClickSound(440, 0.3, 0.5); // A4 note, warm and welcoming
    }
    
    // Generate mobile error beep (system alert style)
    private byte[] generateMobileErrorBeep() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Double beep pattern like iOS error
        byte[] beep1 = generateMobileClickSound(800, 0.1, 0.6);
        byte[] silence = new byte[2200]; // 50ms silence
        byte[] beep2 = generateMobileClickSound(800, 0.1, 0.6);
        
        baos.write(beep1, 0, beep1.length);
        baos.write(silence, 0, silence.length);
        baos.write(beep2, 0, beep2.length);
        
        return baos.toByteArray();
    }
    
    // Create audio clip from byte buffer
    private Clip createClipFromBuffer(byte[] buffer) throws Exception {
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        AudioInputStream audioInputStream = new AudioInputStream(bais, format, buffer.length / 2);
        
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        return clip;
    }
    
    // Cleanup resources
    public void cleanup() {
        soundExecutor.shutdown();
        for (Clip clip : soundClips.values()) {
            if (clip != null) {
                clip.close();
            }
        }
        soundClips.clear();
    }
    
    // Getters (background music getters removed)
    public boolean isSoundEnabled() { return soundEnabled; }
    public float getMasterVolume() { return masterVolume; }
}
