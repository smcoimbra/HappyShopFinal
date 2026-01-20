package ci553.happyshop.client.login;

import ci553.happyshop.utility.UIStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * View class for the login system.
 * Provides the user interface for authentication.
 */
public class LoginView {
    public LoginController loginController;

    private Stage window;
    private TextField tfUsername;
    private PasswordField pfPassword;
    private Label lblMessage;
    private Button btnLogin;

    private static final int LOGIN_WIN_WIDTH = 450;
    private static final int LOGIN_WIN_HEIGHT = 400;

    /**
     * Creates a new LoginView instance.
     */
    public LoginView() {
    }

    /**
     * Starts the login window and displays the UI.
     *
     * @param window The Stage to display the login interface
     */
    public void start(Stage window) {
        this.window = window;

        // Create UI components
        Label lblTitle = new Label("🛒 HappyShop Login 🛒");
        lblTitle.setStyle(UIStyle.labelTitleStyle);

        Label lblUsername = new Label("Username:");
        lblUsername.setStyle(UIStyle.labelStyle);

        tfUsername = new TextField();
        tfUsername.setPromptText("Enter username");
        tfUsername.setStyle(UIStyle.textFiledStyle);
        tfUsername.setPrefWidth(300);

        Label lblPassword = new Label("Password:");
        lblPassword.setStyle(UIStyle.labelStyle);

        pfPassword = new PasswordField();
        pfPassword.setPromptText("Enter password");
        pfPassword.setStyle(UIStyle.textFiledStyle);
        pfPassword.setPrefWidth(300);

        // Add Enter key support for password field
        pfPassword.setOnAction(e -> handleLogin());

        btnLogin = new Button("Login");
        btnLogin.setStyle(UIStyle.buttonStyle);
        btnLogin.setPrefWidth(120);
        btnLogin.setOnAction(e -> handleLogin());

        lblMessage = new Label("");
        lblMessage.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        lblMessage.setWrapText(true);
        lblMessage.setMaxWidth(350);

        // Layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle(UIStyle.rootStyleBlue);
        layout.getChildren().addAll(
                lblTitle,
                lblUsername,
                tfUsername,
                lblPassword,
                pfPassword,
                btnLogin,
                lblMessage
        );

        // Create scene and show window
        Scene scene = new Scene(layout, LOGIN_WIN_WIDTH, LOGIN_WIN_HEIGHT);
        window.setScene(scene);
        window.setTitle("HappyShop Login");
        window.setResizable(false);
        window.show();

        // Focus on username field
        tfUsername.requestFocus();
    }

    /**
     * Handles the login button click or Enter key press.
     */
    private void handleLogin() {
        if (loginController != null) {
            String username = tfUsername.getText();
            String password = pfPassword.getText();
            loginController.doLogin(username, password);
        }
    }

    /**
     * Displays an error message to the user.
     *
     * @param message The error message to display
     */
    public void displayError(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: red; -fx-font-size: 12px; -fx-font-weight: bold;");
    }

    /**
     * Closes the login window.
     */
    public void closeLoginWindow() {
        if (window != null) {
            window.close();
        }
    }

    /**
     * Clears the password field (for security after failed login).
     */
    public void clearPassword() {
        pfPassword.clear();
    }

    /**
     * Gets the Stage window.
     *
     * @return The Stage window
     */
    public Stage getWindow() {
        return window;
    }
}
