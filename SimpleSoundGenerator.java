import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

/**
 * Simple sound generator for TokerGjik game sounds
 * Creates basic sound effects using sine waves
 */
public class SimpleSoundGenerator {
    
    private static final int SAMPLE_RATE = 44100;
    // Default duration removed - each sound now has custom duration for mobile experience
    
    public static void main(String[] args) {
        try {
            // Create sounds directory
            Files.createDirectories(Paths.get("sounds"));
            
            // Generate game sounds
            generatePiecePlace();
            generatePieceMove();
            generateMillFormed();
            generatePieceCapture();
            generateButtonClick();
            generateError();
            
            System.out.println("✅ Sound files generated successfully in 'sounds' directory!");
            
        } catch (Exception e) {
            System.err.println("❌ Error generating sounds: " + e.getMessage());
        }
    }
    
    private static void generatePiecePlace() throws Exception {
        generateMobileClickTone(1200, 80, "sounds/piece_place.wav");
        System.out.println("Generated: piece_place.wav (mobile-style tap)");
    }
    
    private static void generatePieceMove() throws Exception {
        generateMobileSwipeTone(800, 120, "sounds/piece_move.wav");
        System.out.println("Generated: piece_move.wav (mobile-style swipe)");
    }
    
    private static void generateMillFormed() throws Exception {
        generateMobileSuccessChime("sounds/mill_formed.wav");
        System.out.println("Generated: mill_formed.wav (mobile-style success)");
    }
    
    private static void generatePieceCapture() throws Exception {
        generateMobilePopTone(600, 150, "sounds/piece_capture.wav");
        System.out.println("Generated: piece_capture.wav (mobile-style pop)");
    }
    
    private static void generateButtonClick() throws Exception {
        generateMobileClickTone(1000, 60, "sounds/button_click.wav");
        System.out.println("Generated: button_click.wav (mobile-style button)");
    }
    
    private static void generateError() throws Exception {
        generateMobileErrorBeep("sounds/error.wav");
        System.out.println("Generated: error.wav (mobile-style error)");
    }
    
    // Mobile-style click sound (sharp attack, quick decay like iOS tap)
    private static void generateMobileClickTone(int frequency, int durationMs, String filename) throws Exception {
        int samples = (int) ((long) SAMPLE_RATE * durationMs / 1000);
        byte[] buffer = new byte[samples * 2];
        
        for (int i = 0; i < samples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double envelope = Math.exp(-t * 15); // Quick exponential decay
            
            // Mix fundamental with harmonics for richer mobile sound
            double amplitude = envelope * 0.6 * (
                Math.sin(2 * Math.PI * frequency * t) * 0.7 +
                Math.sin(2 * Math.PI * frequency * 2 * t) * 0.2 +
                Math.sin(2 * Math.PI * frequency * 3 * t) * 0.1
            );
            
            short sample = (short) (amplitude * Short.MAX_VALUE);
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        saveWaveFile(buffer, filename);
    }
    
    // Mobile-style swipe sound (frequency sweep)
    private static void generateMobileSwipeTone(int startFreq, int durationMs, String filename) throws Exception {
        int samples = (int) ((long) SAMPLE_RATE * durationMs / 1000);
        byte[] buffer = new byte[samples * 2];
        
        int endFreq = (int)(startFreq * 0.7); // Frequency sweep down
        
        for (int i = 0; i < samples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double progress = t / (durationMs / 1000.0);
            
            // Linear frequency sweep with bell-shaped envelope
            double freq = startFreq + (endFreq - startFreq) * progress;
            double envelope = 0.4 * Math.exp(-0.5 * Math.pow((progress - 0.5) / 0.3, 2)); // Gaussian envelope
            
            double amplitude = envelope * Math.sin(2 * Math.PI * freq * t);
            
            short sample = (short) (amplitude * Short.MAX_VALUE);
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        saveWaveFile(buffer, filename);
    }
    
    // Mobile-style pop sound (bubble-like with frequency modulation)
    private static void generateMobilePopTone(int frequency, int durationMs, String filename) throws Exception {
        int samples = (int) ((long) SAMPLE_RATE * durationMs / 1000);
        byte[] buffer = new byte[samples * 2];
        
        for (int i = 0; i < samples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double progress = t / (durationMs / 1000.0);
            
            // Bubble-like frequency modulation
            double freqMod = frequency * (1 + 0.3 * Math.sin(2 * Math.PI * 8 * t));
            double envelope = 0.5 * Math.exp(-progress * 8) * (1 - progress);
            
            double amplitude = envelope * Math.sin(2 * Math.PI * freqMod * t);
            
            short sample = (short) (amplitude * Short.MAX_VALUE);
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        saveWaveFile(buffer, filename);
    }
    
    // Mobile-style success chime (two-tone like iOS notification)
    private static void generateMobileSuccessChime(String filename) throws Exception {
        // Create two-tone chime: C -> E
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // First note: C5 (523.25 Hz)
        byte[] note1 = generateMobileNote(523, 150);
        // Second note: E5 (659.25 Hz)  
        byte[] note2 = generateMobileNote(659, 200);
        
        baos.write(note1);
        baos.write(note2);
        
        saveWaveFile(baos.toByteArray(), filename);
    }
    
    // Mobile-style error beep (double beep like iOS)
    private static void generateMobileErrorBeep(String filename) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Double beep pattern
        byte[] beep1 = generateMobileNote(800, 100);
        byte[] silence = new byte[2200]; // 50ms silence
        byte[] beep2 = generateMobileNote(800, 100);
        
        baos.write(beep1);
        baos.write(silence);
        baos.write(beep2);
        
        saveWaveFile(baos.toByteArray(), filename);
    }
    
    // Helper method to generate a single mobile-style note
    private static byte[] generateMobileNote(int frequency, int durationMs) {
        int samples = (int) ((long) SAMPLE_RATE * durationMs / 1000);
        byte[] buffer = new byte[samples * 2];
        
        for (int i = 0; i < samples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double envelope = Math.exp(-t * 8); // Mobile-style decay
            
            double amplitude = envelope * 0.6 * Math.sin(2 * Math.PI * frequency * t);
            
            short sample = (short) (amplitude * Short.MAX_VALUE);
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        return buffer;
    }
    
    private static void saveWaveFile(byte[] audioData, String filename) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        AudioInputStream ais = new AudioInputStream(bais, format, audioData.length / 2);
        
        File file = new File(filename);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
    }
}
