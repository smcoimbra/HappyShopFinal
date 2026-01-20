package ci553.happyshop.client.login;

import ci553.happyshop.authentication.CredentialStore;
import ci553.happyshop.authentication.UserRole;

/**
 * Model class for the login system.
 * Handles authentication logic and communicates with CredentialStore.
 */
public class LoginModel {
    public LoginView loginView;
    private CredentialStore credentialStore;

    /**
     * Creates a new LoginModel instance.
     */
    public LoginModel() {
        this.credentialStore = CredentialStore.getInstance();
    }

    /**
     * Authenticates a user with the provided credentials.
     * Updates the user's last login timestamp on successful authentication.
     *
     * @param username The username to authenticate
     * @param password The plain-text password to verify
     * @return true if authentication succeeds, false otherwise
     */
    public boolean authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.isEmpty()) {
            return false;
        }

        return credentialStore.validateCredentials(username, password);
    }

    /**
     * Retrieves the role of a user by username.
     *
     * @param username The username to look up
     * @return The UserRole of the user, or null if user doesn't exist
     */
    public UserRole getUserRole(String username) {
        var user = credentialStore.getUser(username);
        return user != null ? user.getRole() : null;
    }
}
