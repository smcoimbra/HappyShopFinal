package ci553.happyshop.utility;

/**
 * Enum representing different sound effects used in the HappyShop application
 */
public enum SoundEffect {
    BUTTON_CLICK("button-click.wav"),
    ADD_TO_TROLLEY("add-to-trolley.wav"),
    CHECKOUT_SUCCESS("checkout-success.wav"),
    ERROR_NOTIFICATION("error-notification.wav"),
    REMOVE_ITEM("remove-item.wav");

    private final String filename;

    SoundEffect(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
