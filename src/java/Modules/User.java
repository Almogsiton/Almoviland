package Modules;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import config.AppConfig;

/**
 * Represents a user in the system.
 * Each user has a unique ID, personal information, role, and registration details.
 */
public class User implements Serializable {

    private String userId;
    private String name;
    private String email;
    private String password;
    private String role;
    private int limitBorrowMax;
    private Date dateRegistration;

    /**
     * Constructs a new User with default values:
     * - A generated UUID for the user ID.
     * - Current date as the registration date.
     * - Default role and borrow limit from AppConfig.
     */
    public User() {
        this.userId = UUID.randomUUID().toString();
        this.dateRegistration = new Date();
        this.role = AppConfig.getDefaultUserRole();
        this.limitBorrowMax = AppConfig.getDefaultBorrowLimit();
    }

    /**
     * Constructs a User with full details.
     *
     * @param userId The unique user ID.
     * @param name User's full name.
     * @param email User's email address.
     * @param password User's hashed password.
     * @param role The role of the user (e.g., "USER" or "ADMIN").
     * @param limitBorrowMax Maximum allowed borrowings.
     * @param dateRegistration Date of registration.
     */
    public User(String userId, String name, String email, String password, String role, int limitBorrowMax, Date dateRegistration) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.limitBorrowMax = limitBorrowMax;
        this.dateRegistration = dateRegistration;
    }

    /**
     * @return The unique ID of the user.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique ID of the user.
     * @param userId The ID to assign.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return The full name of the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the full name of the user.
     * @param name The user's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * @param email The email to assign.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return The user's password (typically hashed).
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password (should be hashed before storing).
     * @param password The password to assign.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return The user's role (e.g., "USER" or "ADMIN").
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     * @param role The role to assign ("USER", "ADMIN", etc.).
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return The maximum number of items the user can borrow simultaneously.
     */
    public int getLimitBorrowMax() {
        return limitBorrowMax;
    }

    /**
     * Sets the user's borrowing limit.
     * @param limitBorrowMax The maximum number of concurrent borrowings.
     */
    public void setLimitBorrowMax(int limitBorrowMax) {
        this.limitBorrowMax = limitBorrowMax;
    }

    /**
     * @return The date the user registered.
     */
    public Date getDateRegistration() {
        return dateRegistration;
    }

    /**
     * Sets the user's registration date.
     * @param dateRegistration The registration date.
     */
    public void setDateRegistration(Date dateRegistration) {
        this.dateRegistration = dateRegistration;
    }
}
