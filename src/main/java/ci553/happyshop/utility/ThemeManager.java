package ci553.happyshop.utility;

import javafx.scene.Scene;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Singleton class for managing visual themes in the HappyShop application.
 * Handles theme switching and CSS application across all scenes.
 */
public class ThemeManager {
    private static ThemeManager instance;

    private Theme currentTheme = Theme.LIGHT;
    private List<Scene> registeredScenes;

    private static final String SETTINGS_DIR = ".happyshop";
    private static final String SETTINGS_FILE = SETTINGS_DIR + "/theme-settings.properties";

    private ThemeManager() {
        registeredScenes = new ArrayList<>();
    }

    /**
     * Get the singleton instance of ThemeManager
     */
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Set the current theme and apply it to all registered scenes
     */
    public void setTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("Theme cannot be null");
        }

        this.currentTheme = theme;
        applyThemeToAllScenes();
        saveTheme();
    }

    /**
     * Get the current theme
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Register a scene to receive theme updates
     */
    public void registerScene(Scene scene) {
        if (scene != null && !registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyThemeToScene(scene);
        }
    }

    /**
     * Unregister a scene from receiving theme updates
     */
    public void unregisterScene(Scene scene) {
        registeredScenes.remove(scene);
    }

    /**
     * Apply current theme to all registered scenes
     */
    private void applyThemeToAllScenes() {
        for (Scene scene : registeredScenes) {
            applyThemeToScene(scene);
        }
    }

    /**
     * Apply current theme to a specific scene
     */
    private void applyThemeToScene(Scene scene) {
        try {
            scene.getStylesheets().clear();
            String cssPath = getClass().getResource("/" + currentTheme.getCssPath()).toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Failed to apply theme " + currentTheme.name() + ": " + e.getMessage());
            // Fall back to no stylesheet on error
        }
    }

    /**
     * Save theme settings to file
     */
    public void saveTheme() {
        try {
            File dir = new File(SETTINGS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            Properties props = new Properties();
            props.setProperty("theme", currentTheme.name());

            try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
                props.store(out, "HappyShop Theme Settings");
            }
        } catch (IOException e) {
            System.err.println("Failed to save theme settings: " + e.getMessage());
        }
    }

    /**
     * Load theme settings from file
     */
    public void loadTheme() {
        try {
            File file = new File(SETTINGS_FILE);
            if (!file.exists()) {
                return; // Use default theme
            }

            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            }

            String themeName = props.getProperty("theme", "LIGHT");
            try {
                currentTheme = Theme.valueOf(themeName);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid theme name in settings: " + themeName);
                currentTheme = Theme.LIGHT;
            }
        } catch (IOException e) {
            System.err.println("Failed to load theme settings: " + e.getMessage());
        }
    }
}
