package ci553.happyshop.client;

import ci553.happyshop.authentication.CredentialStore;
import ci553.happyshop.authentication.UserRole;
import ci553.happyshop.client.customer.*;

import ci553.happyshop.client.emergency.EmergencyExit;
import ci553.happyshop.client.login.LoginController;
import ci553.happyshop.client.login.LoginModel;
import ci553.happyshop.client.login.LoginView;
import ci553.happyshop.client.orderTracker.OrderTracker;
import ci553.happyshop.client.picker.PickerController;
import ci553.happyshop.client.picker.PickerModel;
import ci553.happyshop.client.picker.PickerView;

import ci553.happyshop.client.warehouse.*;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.storageAccess.DatabaseRWFactory;
import ci553.happyshop.utility.AudioManager;
import ci553.happyshop.utility.ThemeManager;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * The Main JavaFX application class. The Main class is executable directly.
 * It serves as a foundation for UI logic and starts all the clients (UI) in one go.
 *
 * This class launches all standalone clients (Customer, Picker, OrderTracker, Warehouse, EmergencyExit)
 * and links them together into a fully working system.
 *
 * It performs essential setup tasks, such as initializing the order map in the OrderHub
 * and registering observers.
 *
 * Note: Each client type can be instantiated multiple times (e.g., calling startCustomerClient() as many times as needed)
 * to simulate a multi-user environment, where multiple clients of the same type interact with the system concurrently.
 *
 * @version 1.0
 * @author  Shine Shan University of Brighton
 */

public class Main extends Application {

    public static void main(String[] args) {
        launch(args); // Launches the JavaFX application and calls the @Override start()
    }

    //starts the system
    @Override
    public void start(Stage window) throws IOException {
        // Initialize audio-visual system before launching clients
        initializeAudioVisualSystem();

        // Initialize CredentialStore and load credentials
        CredentialStore credentialStore = CredentialStore.getInstance();
        credentialStore.loadCredentials();

        // Show login screen
        showLoginScreen(window);
    }

    /**
     * Initialize the audio-visual system (AudioManager and ThemeManager)
     * Load settings and start background music
     */
    private void initializeAudioVisualSystem() {
        // Initialize AudioManager and load settings
        AudioManager audioManager = AudioManager.getInstance();
        audioManager.loadSettings();

        // Initialize ThemeManager and load theme
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.loadTheme();

        // Start background music playback
        audioManager.playBackgroundMusic();
    }

    /**
     * Displays the login screen and sets up authentication flow.
     *
     * @param primaryStage The primary stage for the login window
     */
    private void showLoginScreen(Stage primaryStage) {
        // Create MVC components for login
        LoginView loginView = new LoginView();
        LoginController loginController = new LoginController();
        LoginModel loginModel = new LoginModel();

        // Wire components together
        loginView.loginController = loginController;
        loginController.loginModel = loginModel;
        loginModel.loginView = loginView;

        // Set callback for successful login
        loginController.setOnLoginSuccess((username, role) -> {
            System.out.println("Login successful: " + username + " (" + role + ")");

            // Close login window
            loginView.closeLoginWindow();

            // Launch appropriate client based on role
            launchClientForRole(role);
        });

        // Start login view
        loginView.start(primaryStage);

        // Register login scene with ThemeManager
        if (primaryStage.getScene() != null) {
            ThemeManager.getInstance().registerScene(primaryStage.getScene());
        }
    }

    /**
     * Launches the appropriate client interface based on user role.
     * Also initializes OrderHub for PICKER and TRACKER roles.
     * Starts EmergencyExit for all roles.
     *
     * @param role The user's role determining which client to launch
     */
    private void launchClientForRole(UserRole role) {
        switch (role) {
            case CUSTOMER:
                startCustomerClient();
                break;
            case PICKER:
                startPickerClient();
                startOrderTracker();
                initializeOrderMap();
                break;
            case WAREHOUSE:
                startWarehouseClient();
                break;
            case TRACKER:
                startOrderTracker();
                initializeOrderMap();
                break;
            default:
                System.err.println("Unknown user role: " + role);
                return;
        }

        // Start EmergencyExit for all roles
        startEmergencyExit();
    }

    /** The customer GUI -search prodduct, add to trolley, cancel/submit trolley, view receipt
     *
     * Creates the Model, View, and Controller objects, links them together so they can communicate with each other.
     * Also creates the DatabaseRW instance via the DatabaseRWFactory and injects it into the CustomerModel.
     * Starts the customer interface.
     *
     * Also creates the RemoveProductNotifier, which tracks the position of the Customer View
     * and is triggered by the Customer Model when needed.
     */
    private void startCustomerClient(){
        CustomerView cusView = new CustomerView();
        CustomerController cusController = new CustomerController();
        CustomerModel cusModel = new CustomerModel();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();

        cusView.cusController = cusController;
        cusController.cusModel = cusModel;
        cusModel.cusView = cusView;
        cusModel.databaseRW = databaseRW;

        Stage stage = new Stage();
        cusView.start(stage);

        // Register scene with ThemeManager
        if (stage.getScene() != null) {
            ThemeManager.getInstance().registerScene(stage.getScene());
        }

        //RemoveProductNotifier removeProductNotifier = new RemoveProductNotifier();
        //removeProductNotifier.cusView = cusView;
        //cusModel.removeProductNotifier = removeProductNotifier;
    }

    /** The picker GUI, - for staff to pack customer's order,
     *
     * Creates the Model, View, and Controller objects for the Picker client.
     * Links them together so they can communicate with each other.
     * Starts the Picker interface.
     *
     * Also registers the PickerModel with the OrderHub to receive order notifications.
     */
    private void startPickerClient(){
        PickerModel pickerModel = new PickerModel();
        PickerView pickerView = new PickerView();
        PickerController pickerController = new PickerController();
        pickerView.pickerController = pickerController;
        pickerController.pickerModel = pickerModel;
        pickerModel.pickerView = pickerView;
        pickerModel.registerWithOrderHub();

        Stage stage = new Stage();
        pickerView.start(stage);

        // Register scene with ThemeManager
        if (stage.getScene() != null) {
            ThemeManager.getInstance().registerScene(stage.getScene());
        }
    }

    //The OrderTracker GUI - for customer to track their order's state(Ordered, Progressing, Collected)
    //This client is simple and does not follow the MVC pattern, as it only registers with the OrderHub
    //to receive order status notifications. All logic is handled internally within the OrderTracker.
    private void startOrderTracker(){
        OrderTracker orderTracker = new OrderTracker();
        orderTracker.registerWithOrderHub();

        // Register scene with ThemeManager
        // Note: OrderTracker creates its own Stage internally, so we need to get it
        if (orderTracker.getScene() != null) {
            ThemeManager.getInstance().registerScene(orderTracker.getScene());
        }
    }

    //initialize the orderMap<orderId, orderState> for OrderHub during system startup
    private void initializeOrderMap(){
        OrderHub orderHub = OrderHub.getOrderHub();
        orderHub.initializeOrderMap();
    }

    /** The Warehouse GUI- for warehouse staff to manage stock
     * Initializes the Warehouse client's Model, View, and Controller,and links them together for communication.
     * It also creates the DatabaseRW instance via the DatabaseRWFactory and injects it into the Model.
     * Once the components are linked, the warehouse interface (view) is started.
     *
     * Also creates the dependent HistoryWindow and AlertSimulator,
     * which track the position of the Warehouse window and are triggered by the Model when needed.
     * These components are linked after launching the Warehouse interface.
     */
    private void startWarehouseClient(){
        WarehouseView view = new WarehouseView();
        WarehouseController controller = new WarehouseController();
        WarehouseModel model = new WarehouseModel();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();

        // Link controller, model, and view and start view
        view.controller = controller;
        controller.model = model;
        model.view = view;
        model.databaseRW = databaseRW;

        Stage stage = new Stage();
        view.start(stage);

        // Register scene with ThemeManager
        if (stage.getScene() != null) {
            ThemeManager.getInstance().registerScene(stage.getScene());
        }

        //create dependent views that need window info
        HistoryWindow historyWindow = new HistoryWindow();
        AlertSimulator alertSimulator = new AlertSimulator();

        // Link after start
        model.historyWindow = historyWindow;
        model.alertSimulator = alertSimulator;
        historyWindow.warehouseView = view;
        alertSimulator.warehouseView = view;

        // Register dependent window scenes with ThemeManager
        if (historyWindow.getScene() != null) {
            ThemeManager.getInstance().registerScene(historyWindow.getScene());
        }
        if (alertSimulator.getScene() != null) {
            ThemeManager.getInstance().registerScene(alertSimulator.getScene());
        }
    }

    //starts the EmergencyExit GUI, - used to close the entire application immediatelly
    private void startEmergencyExit(){
        EmergencyExit emergencyExit = EmergencyExit.getEmergencyExit();

        // Register scene with ThemeManager
        if (emergencyExit.getScene() != null) {
            ThemeManager.getInstance().registerScene(emergencyExit.getScene());
        }
    }
}



