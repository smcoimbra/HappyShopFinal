package ci553.happyshop.utility;


import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Singleton class for managing audio playback in the HappyShop application.
 * Handles background music and sound effects.
 */
public class AudioManager {
    private static AudioManager instance;

    private MediaPlayer backgroundMusicPlayer;
    private Map<SoundEffect, AudioClip> soundEffects;

    private double musicVolume = 0.5;
    private double effectsVolume = 0.7;
    private boolean musicEnabled = true;
    private boolean effectsEnabled = true;

    private static final String SETTINGS_DIR = ".happyshop";
    private static final String SETTINGS_FILE = SETTINGS_DIR + "/audio-settings.properties";

    private AudioManager() {
        soundEffects = new HashMap<>();
        initializeAudio();
    }

    /**
     * Get the singleton instance of AudioManager
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Initialize audio resources
     */
    private void initializeAudio() {
        try {
            // Load background music
            String musicPath = getClass().getResource("/audio/background-music.mp3").toExternalForm();
            Media media = new Media(musicPath);
            backgroundMusicPlayer = new MediaPlayer(media);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(musicVolume);
        } catch (Exception e) {
            System.err.println("Failed to load background music: " + e.getMessage());
            musicEnabled = false;
        }

        // Load sound effects
        for (SoundEffect effect : SoundEffect.values()) {
            try {
                String effectPath = getClass().getResource("/audio/effects/" + effect.getFilename()).toExternalForm();
                AudioClip clip = new AudioClip(effectPath);
                clip.setVolume(effectsVolume);
                soundEffects.put(effect, clip);
            } catch (Exception e) {
                System.err.println("Failed to load sound effect " + effect.name() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Play background music
     */
    public void playBackgroundMusic() {
        if (musicEnabled && backgroundMusicPlayer != null) {
            backgroundMusicPlayer.play();
        }
    }

    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }
    }

    /**
     * Set music volume (0.0 to 1.0)
     */
    public void setMusicVolume(double volume) {
        if (volume < 0.0 || volume > 1.0) {
            throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
        }
        this.musicVolume = volume;
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(volume);
        }
    }

    /**
     * Toggle music on/off
     */
    public void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (musicEnabled) {
            playBackgroundMusic();
        } else {
            stopBackgroundMusic();
        }
    }

    /**
     * Play a sound effect
     */
    public void playEffect(SoundEffect effect) {
        if (effectsEnabled && soundEffects.containsKey(effect)) {
            AudioClip clip = soundEffects.get(effect);
            clip.play();
        }
    }

    /**
     * Set effects volume (0.0 to 1.0)
     */
    public void setEffectsVolume(double volume) {
        if (volume < 0.0 || volume > 1.0) {
            throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
        }
        this.effectsVolume = volume;
        for (AudioClip clip : soundEffects.values()) {
            clip.setVolume(volume);
        }
    }

    /**
     * Toggle sound effects on/off
     */
    public void toggleEffects() {
        effectsEnabled = !effectsEnabled;
    }

    /**
     * Save audio settings to file
     */
    public void saveSettings() {
        try {
            File dir = new File(SETTINGS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            Properties props = new Properties();
            props.setProperty("musicVolume", String.valueOf(musicVolume));
            props.setProperty("effectsVolume", String.valueOf(effectsVolume));
            props.setProperty("musicEnabled", String.valueOf(musicEnabled));
            props.setProperty("effectsEnabled", String.valueOf(effectsEnabled));

            try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
                props.store(out, "HappyShop Audio Settings");
            }
        } catch (IOException e) {
            System.err.println("Failed to save audio settings: " + e.getMessage());
        }
    }

    /**
     * Load audio settings from file
     */
    public void loadSettings() {
        try {
            File file = new File(SETTINGS_FILE);
            if (!file.exists()) {
                return; // Use defaults
            }

            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            }

            musicVolume = Double.parseDouble(props.getProperty("musicVolume", "0.5"));
            effectsVolume = Double.parseDouble(props.getProperty("effectsVolume", "0.7"));
            musicEnabled = Boolean.parseBoolean(props.getProperty("musicEnabled", "true"));
            effectsEnabled = Boolean.parseBoolean(props.getProperty("effectsEnabled", "true"));

            // Apply loaded settings
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.setVolume(musicVolume);
            }
            for (AudioClip clip : soundEffects.values()) {
                clip.setVolume(effectsVolume);
            }
        } catch (IOException e) {
            System.err.println("Failed to load audio settings: " + e.getMessage());
        }
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isEffectsEnabled() {
        return effectsEnabled;
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public double getEffectsVolume() {
        return effectsVolume;
    }
}
