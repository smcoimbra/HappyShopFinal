package ci553.happyshop.utility;

/**
 * Data class for storing theme settings
 */
public class ThemeSettings {
    private Theme selectedTheme;

    /**
     * Create ThemeSettings with default theme (LIGHT)
     */
    public ThemeSettings() {
        this.selectedTheme = Theme.LIGHT;
    }

    /**
     * Create ThemeSettings with specified theme
     */
    public ThemeSettings(Theme selectedTheme) {
        this.selectedTheme = selectedTheme != null ? selectedTheme : Theme.LIGHT;
    }

    /**
     * Get the selected theme
     */
    public Theme getSelectedTheme() {
        return selectedTheme;
    }

    /**
     * Set the selected theme
     */
    public void setSelectedTheme(Theme selectedTheme) {
        this.selectedTheme = selectedTheme != null ? selectedTheme : Theme.LIGHT;
    }
}
