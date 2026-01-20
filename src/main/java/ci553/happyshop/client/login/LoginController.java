package ci553.happyshop.client.login;

import ci553.happyshop.authentication.UserRole;
import java.util.function.BiConsumer;

/**
 * Controller class for the login system.
 * Handles user interactions and coordinates between LoginView and LoginModel.
 */
public class LoginController {
    public LoginModel loginModel;
    private BiConsumer<String, UserRole> onLoginSuccess;

    /**
     * Creates a new LoginController instance.
     */
    public LoginController() {
    }

    /**
     * Sets the callback to be invoked when login succeeds.
     *
     * @param callback A BiConsumer that accepts username and UserRole
     */
    public void setOnLoginSuccess(BiConsumer<String, UserRole> callback) {
        this.onLoginSuccess = callback;
    }

    /**
     * Attempts to log in with the provided credentials.
     * Calls the onLoginSuccess callback if authentication succeeds,
     * or displays an error message in the view if it fails.
     *
     * @param username The username entered by the user
     * @param password The password entered by the user
     */
    public void doLogin(String username, String password) {
        if (loginModel == null) {
            System.err.println("LoginModel is not initialized");
            return;
        }

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            loginModel.loginView.displayError("Please enter a username");
            return;
        }

        if (password == null || password.isEmpty()) {
            loginModel.loginView.displayError("Please enter a password");
            return;
        }

        // Attempt authentication
        boolean authenticated = loginModel.authenticate(username, password);

        if (authenticated) {
            // Get user role
            UserRole role = loginModel.getUserRole(username);

            if (role != null && onLoginSuccess != null) {
                // Trigger success callback
                onLoginSuccess.accept(username, role);
            } else {
                loginModel.loginView.displayError("Unable to determine user role");
            }
        } else {
            // Display error message
            loginModel.loginView.displayError("Invalid username or password");
        }
    }
}
