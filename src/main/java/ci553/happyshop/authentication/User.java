package ci553.happyshop.authentication;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Has user credentials , information about the roles , and login tracking.*/

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String passwordHash;
    private UserRole role;
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;

    /**
     * here you can create a new user.
     *  username :  The unique username for this user
     *  passwordHash :  password
     */
    public User(String username, String passwordHash, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdDate = LocalDateTime.now();
        this.lastLogin = null;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role=" + role +
                ", createdDate=" + createdDate +
                ", lastLogin=" + lastLogin +
                '}';
    }
}
