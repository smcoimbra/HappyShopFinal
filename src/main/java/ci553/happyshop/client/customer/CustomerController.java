package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.utility.AudioManager;
import ci553.happyshop.utility.SoundEffect;

import java.io.IOException;
import java.sql.SQLException;

public class CustomerController {
    public CustomerModel cusModel;

    public void doAction(String action) throws SQLException, IOException {
        // Play button click sound for all button actions
        AudioManager.getInstance().playEffect(SoundEffect.BUTTON_CLICK);

        switch (action) {
            case "Search":
                cusModel.search();
                break;
            case "Add to Trolley":
                cusModel.addToTrolley();
                break;
            case "Cancel":
                cusModel.cancel();
                break;
            case "Check Out":
                cusModel.checkOut();
                break;
            case "OK & Close":
                cusModel.closeReceipt();
                break;
        }
    }

    /**
     * Handles product selection from the search results ListView.
     * Called when user clicks on a product in the search results.
     *
     * @param product the selected product
     */
    public void selectProduct(Product product) {
        cusModel.selectProduct(product);
    }

    /**
     * Updates the quantity of a product in the trolley.
     * Called when user changes the quantity spinner in the trolley.
     *
     * @param product the product to update
     * @param newQuantity the new quantity value
     */
    public void updateQuantity(Product product, int newQuantity) {
        cusModel.updateProductQuantity(product, newQuantity);
    }

    /**
     * Removes a product from the trolley.
     * Called when user clicks the remove button in the trolley.
     *
     * @param product the product to remove
     */
    public void removeFromTrolley(Product product) {
        cusModel.removeProduct(product);
    }

}

