package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.AudioManager;
import ci553.happyshop.utility.SoundEffect;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 * You can either directly modify the CustomerModel class to implement the required tasks,
 * or create a subclass of CustomerModel and override specific methods where appropriate.
 */
public class CustomerModel {
    public CustomerView cusView;
    public DatabaseRW databaseRW; //Interface type, not specific implementation
    //Benefits: Flexibility: Easily change the database implementation.

    private Product theProduct =null; // product found from search
    private ArrayList<Product> trolley =  new ArrayList<>(); // a list of products in trolley

    // Four UI elements to be passed to CustomerView for display updates.
    private String imageName = "imageHolder.jpg";                // Image to show in product preview (Search Page)
    private String displayLaSearchResult = "No Product was searched yet"; // Label showing search result message (Search Page)
    private String displayTaTrolley = "";                                // Text area content showing current trolley items (Trolley Page)
    private String displayTaReceipt = "";                                // Text area content showing receipt after checkout (Receipt Page)

    //SELECT productID, description, image, unitPrice,inStock quantity
    void search() throws SQLException {
        String keyword = cusView.tfSearch.getText().trim();

        if (keyword.isEmpty()) {
            theProduct = null;
            displayLaSearchResult = "Please enter a search term";
            System.out.println("Please enter a search term.");
            AudioManager.getInstance().playEffect(SoundEffect.ERROR_NOTIFICATION);
            cusView.lvSearchResults.getItems().clear();
            updateView();
            return;
        }

        // Use existing searchProduct method from DatabaseRW which handles both ID and name
        ArrayList<Product> results = databaseRW.searchProduct(keyword);

        if (results.isEmpty()) {
            // Handle no results
            theProduct = null;
            displayLaSearchResult = "No products found for: " + keyword;
            System.out.println("No products found for: " + keyword);
            AudioManager.getInstance().playEffect(SoundEffect.ERROR_NOTIFICATION);
            cusView.lvSearchResults.getItems().clear();
        } else if (results.size() == 1) {
            // Handle single result - display product immediately
            theProduct = results.get(0);
            displayProductInfo(theProduct);
            cusView.lvSearchResults.getItems().clear();
        } else {
            // Handle multiple results - populate ListView in view
            cusView.lvSearchResults.getItems().setAll(results);
            displayLaSearchResult = results.size() + " products found. Click to select.";
            System.out.println(results.size() + " products found");
            theProduct = null; // Clear current product until user selects one
        }

        updateView();
    }

    /**
     * Helper method to display product information
     */
    private void displayProductInfo(Product product) {
        if (product != null && product.getStockQuantity() > 0) {
            double unitPrice = product.getUnitPrice();
            String description = product.getProductDescription();
            int stock = product.getStockQuantity();
            String productId = product.getProductId();

            String baseInfo = String.format("Product_Id: %s\n%s,\nPrice: £%.2f", productId, description, unitPrice);
            String quantityInfo = stock < 100 ? String.format("\n%d units left.", stock) : "";
            displayLaSearchResult = baseInfo + quantityInfo;
            System.out.println(displayLaSearchResult);
        } else if (product != null && product.getStockQuantity() == 0) {
            displayLaSearchResult = "Product " + product.getProductId() + " is out of stock";
            System.out.println("Product is out of stock");
            AudioManager.getInstance().playEffect(SoundEffect.ERROR_NOTIFICATION);
        }
    }

    /**
     * Selects a product from the search results and displays its details.
     * Called when user clicks on a product in the ListView.
     *
     * @param product the selected product
     */
    void selectProduct(Product product) {
        theProduct = product;
        displayProductInfo(product);
        cusView.lvSearchResults.getItems().clear(); // Clear the list after selection
        updateView();
    }

    void addToTrolley(){
        if(theProduct!= null){
            // Read quantity from spinner
            int requestedQty = cusView.spnSearchQuantity.getValue();

            // Validate requested quantity against available stock
            if (requestedQty > theProduct.getStockQuantity()) {
                displayLaSearchResult = String.format(
                        "Only %d units available for %s. Please reduce quantity.",
                        theProduct.getStockQuantity(),
                        theProduct.getProductId()
                );
                System.out.println("Requested quantity exceeds available stock");
                AudioManager.getInstance().playEffect(SoundEffect.ERROR_NOTIFICATION);
                updateView();
                return;
            }

            // Create product with requested quantity
            Product productToAdd = new Product(
                    theProduct.getProductId(),
                    theProduct.getProductDescription(),
                    theProduct.getProductImageName(),
                    theProduct.getUnitPrice(),
                    theProduct.getStockQuantity()
            );
            productToAdd.setOrderedQuantity(requestedQty);

            // Add product to trolley
            trolley.add(productToAdd);

            // Organize trolley: merge duplicates and sort by Product ID
            organizeTrolley();

            // Update display with organized trolley
            displayTaTrolley = ProductListFormatter.buildString(trolley);
            AudioManager.getInstance().playEffect(SoundEffect.ADD_TO_TROLLEY);

            // Reset spinner to 1 after successful add
            cusView.spnSearchQuantity.getValueFactory().setValue(1);
        }
        else{
            displayLaSearchResult = "Please search for an available product before adding it to the trolley";
            System.out.println("must search and get an available product before add to trolley");
            AudioManager.getInstance().playEffect(SoundEffect.ERROR_NOTIFICATION);
        }
        displayTaReceipt=""; // Clear receipt to switch back to trolleyPage (receipt shows only when not empty)
        updateView();
    }

    void checkOut() throws IOException, SQLException {
        if(!trolley.isEmpty()){
            // Group the products in the trolley by productId to optimize stock checking
            // Check the database for sufficient stock for all products in the trolley.
            // If any products are insufficient, the update will be rolled back.
            // If all products are sufficient, the database will be updated, and insufficientProducts will be empty.
            // Note: Since the trolley is now organized (merged and sorted by organizeTrolley()),
            // grouping is redundant but kept for safety and backward compatibility.
            ArrayList<Product> groupedTrolley= groupProductsById(trolley);
            ArrayList<Product> insufficientProducts= databaseRW.purchaseStocks(groupedTrolley);

            if(insufficientProducts.isEmpty()){ // If stock is sufficient for all products
                //get OrderHub and tell it to make a new Order
                OrderHub orderHub =OrderHub.getOrderHub();
                Order theOrder = orderHub.newOrder(trolley);
                trolley.clear();
                displayTaTrolley ="";
                displayTaReceipt = String.format(
                        "Order_ID: %s\nOrdered_Date_Time: %s\n%s",
                        theOrder.getOrderId(),
                        theOrder.getOrderedDateTime(),
                        ProductListFormatter.buildString(theOrder.getProductList())
                );
                System.out.println(displayTaReceipt);
                AudioManager.getInstance().playEffect(SoundEffect.CHECKOUT_SUCCESS);
            }
            else{ // Some products have insufficient stock — build an error message to inform the customer
                StringBuilder errorMsg = new StringBuilder();
                for(Product p : insufficientProducts){
                    errorMsg.append("\u2022 "+ p.getProductId()).append(", ")
                            .append(p.getProductDescription()).append(" (Only ")
                            .append(p.getStockQuantity()).append(" available, ")
                            .append(p.getOrderedQuantity()).append(" requested)\n");
                }
                theProduct=null;

                //TODO
                // Add the following logic here:
                // 1. Remove products with insufficient stock from the trolley.
                // 2. Trigger a message window to notify the customer about the insufficient stock, rather than directly changing displayLaSearchResult.
                //You can use the provided RemoveProductNotifier class and its showRemovalMsg method for this purpose.
                //remember close the message window where appropriate (using method closeNotifierWindow() of RemoveProductNotifier class)
                displayLaSearchResult = "Checkout failed due to insufficient stock for the following products:\n" + errorMsg.toString();
                System.out.println("stock is not enough");
                AudioManager.getInstance().playEffect(SoundEffect.ERROR_NOTIFICATION);
            }
        }
        else{
            displayTaTrolley = "Your trolley is empty";
            System.out.println("Your trolley is empty");
            AudioManager.getInstance().playEffect(SoundEffect.ERROR_NOTIFICATION);
        }
        updateView();
    }

    /**
     * Organizes the trolley by merging duplicate products and sorting by Product ID.
     * This method:
     * 1. Merges products with the same Product ID by summing their ordered quantities
     * 2. Sorts the merged products by Product ID in ascending order
     * 3. Replaces the trolley with the organized version
     */
    private void organizeTrolley() {
        // Step 1: Merge duplicates using HashMap
        Map<String, Product> merged = new HashMap<>();
        for (Product p : trolley) {
            String id = p.getProductId();
            if (merged.containsKey(id)) {
                // Product already exists - sum the quantities
                Product existing = merged.get(id);
                existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                // New product - add to map
                merged.put(id, p);
            }
        }

        // Step 2: Convert to ArrayList and sort by Product ID
        trolley = new ArrayList<>(merged.values());
        java.util.Collections.sort(trolley); // Uses Product.compareTo() which sorts by Product ID
    }

    /**
     * Groups products by their productId to optimize database queries and updates.
     * By grouping products, we can check the stock for a given `productId` once, rather than repeatedly
     */
    private ArrayList<Product> groupProductsById(ArrayList<Product> proList) {
        Map<String, Product> grouped = new HashMap<>();
        for (Product p : proList) {
            String id = p.getProductId();
            if (grouped.containsKey(id)) {
                Product existing = grouped.get(id);
                existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                // Make a shallow copy to avoid modifying the original
                grouped.put(id,new Product(p.getProductId(),p.getProductDescription(),
                        p.getProductImageName(),p.getUnitPrice(),p.getStockQuantity()));
            }
        }
        return new ArrayList<>(grouped.values());
    }

    /**
     * Updates the quantity of a specific product in the trolley.
     * Finds the product by Product ID and updates its ordered quantity.
     *
     * @param product the product to update
     * @param newQuantity the new quantity value
     */
    void updateProductQuantity(Product product, int newQuantity) {
        // Find product in trolley by Product ID and update orderedQuantity
        for (Product p : trolley) {
            if (p.getProductId().equals(product.getProductId())) {
                p.setOrderedQuantity(newQuantity);
                break;
            }
        }

        // Recalculate trolley display string (for backward compatibility if needed)
        displayTaTrolley = ProductListFormatter.buildString(trolley);

        // Call updateView() to refresh display
        updateView();
    }

    /**
     * Removes a product from the trolley.
     * Uses removeIf with Product ID match to remove the item.
     *
     * @param product the product to remove
     */
    void removeProduct(Product product) {
        // Remove product from trolley using removeIf with Product ID match
        trolley.removeIf(p -> p.getProductId().equals(product.getProductId()));

        // Call organizeTrolley() to maintain organization
        organizeTrolley();

        // Play REMOVE_ITEM sound effect
        AudioManager.getInstance().playEffect(SoundEffect.REMOVE_ITEM);

        // Display "Your trolley is empty" message if trolley becomes empty
        if (trolley.isEmpty()) {
            displayTaTrolley = "Your trolley is empty";
        } else {
            displayTaTrolley = ProductListFormatter.buildString(trolley);
        }

        updateView();
    }

    void cancel(){
        trolley.clear();
        displayTaTrolley="";
        updateView();
    }
    void closeReceipt(){
        displayTaReceipt="";
    }

    void updateView() {
        if(theProduct != null){
            imageName = theProduct.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder +imageName; //relative file path, eg images/0001.jpg
            // Get the full absolute path to the image
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            imageName = imageFullPath.toUri().toString(); //get the image full Uri then convert to String
            System.out.println("Image absolute path: " + imageFullPath); // Debugging to ensure path is correct
        }
        else{
            imageName = "imageHolder.jpg";
        }
        cusView.update(imageName, displayLaSearchResult, displayTaReceipt);
        cusView.updateTrolley(trolley);
    }
    // extra notes:
    //Path.toUri(): Converts a Path object (a file or a directory path) to a URI object.
    //File.toURI(): Converts a File object (a file on the filesystem) to a URI object

    //for test only
    public ArrayList<Product> getTrolley() {
        return trolley;
    }
}
