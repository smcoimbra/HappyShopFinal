package ci553.happyshop.utility;

/**
 * Enum representing different visual themes for the HappyShop application
 */
public enum Theme {
    LIGHT("styles/light-theme.css"),
    DARK("styles/dark-theme.css"),
    COLORFUL("styles/colorful-theme.css");

    private final String cssPath;

    Theme(String cssPath) {
        this.cssPath = cssPath;
    }

    public String getCssPath() {
        return cssPath;
    }
}
