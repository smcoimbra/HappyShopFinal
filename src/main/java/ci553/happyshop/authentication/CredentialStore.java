package ci553.happyshop.authentication;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**validation and managing the users.*/
public class CredentialStore {
    private static CredentialStore instance;
    private Map<String, User> users;
    private static final String CREDENTIALS_FILE = ".happyshop/credentials.dat";


    private CredentialStore() {
        users = new HashMap<>();
    }


    public static synchronized CredentialStore getInstance() {
        if (instance == null) {
            instance = new CredentialStore();
        }
        return instance;
    }

    /**
     * Here it validates the username and passwrod of the user
     * * username : The username to validate
     *  password : The plain-text password to verify
     *  "true" if credentials are valid, false if not */
    public boolean validateCredentials(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            return false;
        }

        boolean isValid = verifyPassword(password, user.getPasswordHash());
        if (isValid) {
            user.setLastLogin(LocalDateTime.now());
        }

        return isValid;
    }

    public User getUser(String username) {
        return users.get(username);
    }

    /** Hashes the password using SHA-256 that we learned in security. the safest and simplest way.*/
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 algorithm not available: " + e.getMessage());
            // Fallback to simple hash (not secure, but allows system to function)
            return String.valueOf(password.hashCode());
        }
    }

    /*** Verifies a text password with the hash. true if the password matches the hash, false otherwise*/

    public boolean verifyPassword(String password, String hash) {
        String passwordHash = hashPassword(password);
        return passwordHash.equals(hash);
    }

    /** saves all the credentials to storageand it creates the .happyshop directory if it doesn't exist.*/
    public void saveCredentials() {
        File file = new File(CREDENTIALS_FILE);
        File directory = file.getParentFile();

        // Create directory if it doesn't exist
        if (directory != null && !directory.exists()) {
            if (!directory.mkdirs()) {
                System.err.println("Failed to create directory: " + directory.getAbsolutePath());
                return;
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(users);
            System.out.println("Credentials saved successfully to " + CREDENTIALS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving credentials: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** this loads user credentials created above from the storageand it creates default users if the file doesn't exist*/
    @SuppressWarnings("unchecked")
    public void loadCredentials() {
        File file = new File(CREDENTIALS_FILE);

        if (!file.exists()) {
            System.out.println("Credentials file not found. Creating default users...");
            createDefaultUsers();
            saveCredentials();
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (Map<String, User>) ois.readObject();
            System.out.println("Credentials loaded successfully. Total users: " + users.size());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading credentials: " + e.getMessage());
            System.out.println("Creating default users...");
            createDefaultUsers();
            saveCredentials();
        }
    }

    /*** Creates one user for each role :
     *  customer1, picker1, warehouse1, tracker1.
     * divided in different sections and people*/
    private void createDefaultUsers() {
        users.clear();

        // users and passwords
        User customer = new User("customer1", hashPassword("customer123"), UserRole.CUSTOMER);
        User picker = new User("picker1", hashPassword("picker123"), UserRole.PICKER);
        User warehouse = new User("warehouse1", hashPassword("warehouse123"), UserRole.WAREHOUSE);
        User tracker = new User("tracker1", hashPassword("tracker123"), UserRole.TRACKER);

        users.put(customer.getUsername(), customer);
        users.put(picker.getUsername(), picker);
        users.put(warehouse.getUsername(), warehouse);
        users.put(tracker.getUsername(), tracker);

        System.out.println("Default users created:");
        System.out.println("  - customer1 / customer123 (CUSTOMER)");
        System.out.println("  - picker1 / picker123 (PICKER)");
        System.out.println("  - warehouse1 / warehouse123 (WAREHOUSE)");
        System.out.println("  - tracker1 / tracker123 (TRACKER)");
    }

    /*** adds a new user to the credential store.*/
    public boolean addUser(String username, String password, UserRole role) {
        if (users.containsKey(username)) {
            System.err.println("User already exists: " + username);
            return false;
        }

        String passwordHash = hashPassword(password);
        User newUser = new User(username, passwordHash, role);
        users.put(username, newUser);
        saveCredentials();

        System.out.println("User added successfully: " + username + " (" + role + ")");
        return true;
    }

    /*** updates the password of any user*/
    public boolean updateUser(String username, String newPassword) {
        User user = users.get(username);
        if (user == null) {
            System.err.println("User not found: " + username);
            return false;
        }

        String newPasswordHash = hashPassword(newPassword);
        user.setPasswordHash(newPasswordHash);
        saveCredentials();

        System.out.println("Password updated successfully for user: " + username);
        return true;
    }

    /*** deletes a user from the storage.*/
    public boolean deleteUser(String username) {
        User removedUser = users.remove(username);
        if (removedUser == null) {
            System.err.println("User not found: " + username);
            return false;
        }

        saveCredentials();
        System.out.println("User deleted successfully: " + username);
        return true;
    }
}
