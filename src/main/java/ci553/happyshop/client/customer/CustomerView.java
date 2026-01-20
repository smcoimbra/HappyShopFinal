package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.utility.Theme;
import ci553.happyshop.utility.ThemeManager;
import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WinPosManager;
import ci553.happyshop.utility.WindowBounds;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * The CustomerView is separated into two sections by a line :
 *
 * 1. Search Page – Always visible, allowing customers to browse and search for products.
 * 2. the second page – display either the Trolley Page or the Receipt Page
 *    depending on the current context. Only one of these is shown at a time.
 */

public class CustomerView  {
    public CustomerController cusController;

    private final int WIDTH = UIStyle.customerWinWidth;
    private final int HEIGHT = UIStyle.customerWinHeight;
    private final int COLUMN_WIDTH = WIDTH / 2 - 10;

    private HBox hbRoot; // Top-level layout manager
    private VBox vbTrolleyPage;  //vbTrolleyPage and vbReceiptPage will swap with each other when need
    private VBox vbReceiptPage;

    public TextField tfSearch; //unified search field for product ID or name. Made accessible so it can be accessed by CustomerModel
    public ListView<Product> lvSearchResults; //ListView for displaying multiple search results
    public ListView<Product> lvTrolley; //ListView for displaying trolley items with interactive controls
    public Spinner<Integer> spnSearchQuantity; //Spinner for quantity control in search results

    //four controllers needs updating when program going on
    private ImageView ivProduct; //image area in searchPage
    private Label lbProductInfo;//product text info in searchPage
    private TextArea taReceipt;//in receipt page

    // Holds a reference to this CustomerView window for future access and management
    // (e.g., positioning the removeProductNotifier when needed).
    private Stage viewWindow;

    public void start(Stage window) {
        VBox vbSearchPage = createSearchPage();
        vbTrolleyPage = CreateTrolleyPage();
        vbReceiptPage = createReceiptPage();

        // Create a divider line
        Line line = new Line(0, 0, 0, HEIGHT);
        line.setStrokeWidth(4);
        line.setStroke(Color.PINK);
        VBox lineContainer = new VBox(line);
        lineContainer.setPrefWidth(4); // Give it some space
        lineContainer.setAlignment(Pos.CENTER);

        hbRoot = new HBox(10, vbSearchPage, lineContainer, vbTrolleyPage); //initialize to show trolleyPage
        hbRoot.setAlignment(Pos.CENTER);
        hbRoot.setStyle(UIStyle.rootStyle);

        // Create theme selector in top-right corner
        ComboBox<Theme> cbTheme = createThemeSelector();

        // Create main content with theme selector
        BorderPane root = new BorderPane();
        root.setCenter(hbRoot);
        root.setTop(cbTheme);
        BorderPane.setAlignment(cbTheme, Pos.TOP_RIGHT);
        BorderPane.setMargin(cbTheme, new javafx.geometry.Insets(10, 10, 0, 0));

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Register scene with ThemeManager
        ThemeManager.getInstance().registerScene(scene);

        window.setScene(scene);
        window.setTitle("🛒 HappyShop Customer Client");
        WinPosManager.registerWindow(window,WIDTH,HEIGHT); //calculate position x and y for this window
        window.show();
        viewWindow=window;// Sets viewWindow to this window for future reference and management.
    }

    private VBox createSearchPage() {
        Label laPageTitle = new Label("Search by Product ID/Name");
        laPageTitle.setStyle(UIStyle.labelTitleStyle);

        // Unified search field
        tfSearch = new TextField();
        tfSearch.setPromptText("Enter product ID or name");
        tfSearch.setStyle(UIStyle.textFiledStyle);

        Button btnSearch = new Button("Search");
        btnSearch.setStyle(UIStyle.buttonStyle);
        btnSearch.setOnAction(this::buttonClicked);

        HBox hbSearch = new HBox(10, tfSearch, btnSearch);
        hbSearch.setAlignment(Pos.CENTER_LEFT);

        // ListView for multiple search results
        lvSearchResults = new ListView<>();
        lvSearchResults.setPrefHeight(80);
        lvSearchResults.setStyle(UIStyle.listViewStyle);

        // Set cell factory to display product ID, description, and price
        lvSearchResults.setCellFactory(param -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s - £%.2f (Stock: %d)",
                            product.getProductId(),
                            product.getProductDescription(),
                            product.getUnitPrice(),
                            product.getStockQuantity()));
                }
            }
        });

        // Add mouse click handler to select product from ListView
        lvSearchResults.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                Product selected = lvSearchResults.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    cusController.selectProduct(selected);
                }
            }
        });

        ivProduct = new ImageView("imageHolder.jpg");
        ivProduct.setFitHeight(60);
        ivProduct.setFitWidth(60);
        ivProduct.setPreserveRatio(true); // Image keeps its original shape and fits inside 60×60
        ivProduct.setSmooth(true); //make it smooth and nice-looking

        lbProductInfo = new Label("Thank you for shopping with us.");
        lbProductInfo.setWrapText(true);
        lbProductInfo.setMinHeight(Label.USE_PREF_SIZE);  // Allow auto-resize
        lbProductInfo.setStyle(UIStyle.labelMulLineStyle);
        HBox hbSearchResult = new HBox(5, ivProduct, lbProductInfo);
        hbSearchResult.setAlignment(Pos.CENTER_LEFT);

        // Quantity control for search results
        Label laQuantity = new Label("Quantity:");
        laQuantity.setStyle(UIStyle.labelStyle);
        spnSearchQuantity = new Spinner<>(1, 999, 1);
        spnSearchQuantity.setPrefWidth(80);
        spnSearchQuantity.setEditable(true);
        HBox hbQuantity = new HBox(10, laQuantity, spnSearchQuantity);
        hbQuantity.setAlignment(Pos.CENTER_LEFT);

        Button btnAddToTrolley = new Button("Add to Trolley");
        btnAddToTrolley.setStyle(UIStyle.buttonStyle);
        btnAddToTrolley.setOnAction(this::buttonClicked);

        VBox vbSearchPage = new VBox(15, laPageTitle, hbSearch, lvSearchResults, hbSearchResult, hbQuantity, btnAddToTrolley);
        vbSearchPage.setPrefWidth(COLUMN_WIDTH);
        vbSearchPage.setAlignment(Pos.TOP_CENTER);
        vbSearchPage.setStyle("-fx-padding: 15px;");

        return vbSearchPage;
    }

    private VBox CreateTrolleyPage() {
        Label laPageTitle = new Label("🛒🛒  Trolley 🛒🛒");
        laPageTitle.setStyle(UIStyle.labelTitleStyle);

        // Replace TextArea with ListView<Product>
        lvTrolley = new ListView<>();
        lvTrolley.setPrefSize(WIDTH/2, HEIGHT-50);
        lvTrolley.setStyle(UIStyle.listViewStyle);

        // Set cell factory to TrolleyItemCell
        lvTrolley.setCellFactory(param -> new TrolleyItemCell());

        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(this::buttonClicked);
        btnCancel.setStyle(UIStyle.buttonStyle);

        Button btnCheckout = new Button("Check Out");
        btnCheckout.setOnAction(this::buttonClicked);
        btnCheckout.setStyle(UIStyle.buttonStyle);

        HBox hbBtns = new HBox(10, btnCancel,btnCheckout);
        hbBtns.setStyle("-fx-padding: 15px;");
        hbBtns.setAlignment(Pos.CENTER);

        vbTrolleyPage = new VBox(15, laPageTitle, lvTrolley, hbBtns);
        vbTrolleyPage.setPrefWidth(COLUMN_WIDTH);
        vbTrolleyPage.setAlignment(Pos.TOP_CENTER);
        vbTrolleyPage.setStyle("-fx-padding: 15px;");
        return vbTrolleyPage;
    }

    private VBox createReceiptPage() {
        Label laPageTitle = new Label("Receipt");
        laPageTitle.setStyle(UIStyle.labelTitleStyle);

        taReceipt = new TextArea();
        taReceipt.setEditable(false);
        taReceipt.setPrefSize(WIDTH/2, HEIGHT-50);

        Button btnCloseReceipt = new Button("OK & Close"); //btn for closing receipt and showing trolley page
        btnCloseReceipt.setStyle(UIStyle.buttonStyle);

        btnCloseReceipt.setOnAction(this::buttonClicked);

        vbReceiptPage = new VBox(15, laPageTitle, taReceipt, btnCloseReceipt);
        vbReceiptPage.setPrefWidth(COLUMN_WIDTH);
        vbReceiptPage.setAlignment(Pos.TOP_CENTER);
        vbReceiptPage.setStyle(UIStyle.rootStyleYellow);
        return vbReceiptPage;
    }


    private void buttonClicked(ActionEvent event) {
        try{
            Button btn = (Button)event.getSource();
            String action = btn.getText();
            if(action.equals("Add to Trolley")){
                showTrolleyOrReceiptPage(vbTrolleyPage); //ensure trolleyPage shows if the last customer did not close their receiptPage
            }
            if(action.equals("OK & Close")){
                showTrolleyOrReceiptPage(vbTrolleyPage);
            }
            cusController.doAction(action);
        }
        catch(SQLException e){
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void update(String imageName, String searchResult, String receipt) {
        ivProduct.setImage(new Image(imageName));
        lbProductInfo.setText(searchResult);

        if (!receipt.equals("")) {
            showTrolleyOrReceiptPage(vbReceiptPage);
            taReceipt.setText(receipt);
        }
    }

    /**
     * Updates the trolley ListView with the current trolley items
     */
    public void updateTrolley(ArrayList<Product> trolleyItems) {
        lvTrolley.getItems().setAll(trolleyItems);
    }

    // Replaces the last child of hbRoot with the specified page.
    // the last child is either vbTrolleyPage or vbReceiptPage.
    private void showTrolleyOrReceiptPage(Node pageToShow) {
        int lastIndex = hbRoot.getChildren().size() - 1;
        if (lastIndex >= 0) {
            hbRoot.getChildren().set(lastIndex, pageToShow);
        }
    }

    WindowBounds getWindowBounds() {
        return new WindowBounds(viewWindow.getX(), viewWindow.getY(),
                viewWindow.getWidth(), viewWindow.getHeight());
    }

    /**
     * Create theme selector ComboBox
     */
    private ComboBox<Theme> createThemeSelector() {
        ComboBox<Theme> cbTheme = new ComboBox<>();
        cbTheme.getItems().addAll(Theme.values());
        cbTheme.setValue(ThemeManager.getInstance().getCurrentTheme());
        cbTheme.setStyle(UIStyle.comboBoxStyle);

        // Bind to ThemeManager
        cbTheme.setOnAction(event -> {
            Theme selectedTheme = cbTheme.getValue();
            if (selectedTheme != null) {
                ThemeManager.getInstance().setTheme(selectedTheme);
            }
        });

        return cbTheme;
    }

    /**
     * Custom ListCell for trolley items with quantity control and remove button
     */
    private class TrolleyItemCell extends ListCell<Product> {
        private HBox content;
        private Label lblInfo;
        private Spinner<Integer> spnQuantity;
        private Button btnRemove;

        public TrolleyItemCell() {
            // Label for product info (ID, description, line total)
            lblInfo = new Label();
            lblInfo.setPrefWidth(250);
            lblInfo.setWrapText(true);
            lblInfo.setStyle("-fx-font-size: 14px;");

            // Spinner for quantity control (range 1-999)
            spnQuantity = new Spinner<>(1, 999, 1);
            spnQuantity.setPrefWidth(70);
            spnQuantity.setEditable(true);

            // Add value change listener to Spinner
            spnQuantity.valueProperty().addListener((obs, oldVal, newVal) -> {
                Product product = getItem();
                if (product != null && newVal != null) {
                    cusController.updateQuantity(product, newVal);
                }
            });

            // Remove button with red styling
            btnRemove = new Button("Remove");
            btnRemove.setStyle(UIStyle.redFillBtnStyle);
            btnRemove.setOnAction(e -> {
                Product product = getItem();
                if (product != null) {
                    cusController.removeFromTrolley(product);
                }
            });

            // Layout components in HBox with proper alignment
            content = new HBox(10, lblInfo, spnQuantity, btnRemove);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setStyle("-fx-padding: 5px;");
        }

        @Override
        protected void updateItem(Product product, boolean empty) {
            super.updateItem(product, empty);
            if (empty || product == null) {
                setGraphic(null);
            } else {
                // Display product info: ID, description, line total
                double lineTotal = product.getUnitPrice() * product.getOrderedQuantity();
                lblInfo.setText(String.format("%s - %s\n£%.2f × %d = £%.2f",
                        product.getProductId(),
                        product.getProductDescription(),
                        product.getUnitPrice(),
                        product.getOrderedQuantity(),
                        lineTotal));

                // Set spinner value to current quantity
                spnQuantity.getValueFactory().setValue(product.getOrderedQuantity());

                setGraphic(content);
            }
        }
    }
}
