package DAO;

import Modules.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import config.AppConfig;

/**
 * Data Access Object (DAO) for managing users in the system.
 * Provides methods to create, retrieve, update, and delete user records from the database,
 * as well as utility functions such as password hashing and default admin creation.
 */
public class UserDAO {

    /**
     * Adds a new user to the database.
     *
     * @param user The user to be added.
     * @return true if insertion was successful, false otherwise.
     */
    public static boolean addUser(User user) {
        String sql = "INSERT INTO USERS (USER_ID, NAME, EMAIL, PASSWORD, ROLE, LIMIT_BORROW_MAX, DATE_REGISTRATION) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), 
                                                           AppConfig.getDatabaseUser(), 
                                                           AppConfig.getDatabasePassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, hashPassword(user.getPassword()));
            pstmt.setString(5, user.getRole());
            pstmt.setInt(6, user.getLimitBorrowMax());
            pstmt.setDate(7, new java.sql.Date(System.currentTimeMillis()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a user from the database by their email.
     *
     * @param email The email of the user to retrieve.
     * @return A User object if found, otherwise null.
     */
    public static User getUserByEmail(String email) {
        String sql = "SELECT * FROM USERS WHERE EMAIL = ?";
        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), 
                                                           AppConfig.getDatabaseUser(), 
                                                           AppConfig.getDatabasePassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("USER_ID"),
                        rs.getString("NAME"),
                        rs.getString("EMAIL"),
                        rs.getString("PASSWORD"),
                        rs.getString("ROLE"),
                        rs.getInt("LIMIT_BORROW_MAX"),
                        rs.getDate("DATE_REGISTRATION")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all users from the database.
     *
     * @return A list of all users.
     */
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM USERS";

        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), 
                                                           AppConfig.getDatabaseUser(), 
                                                           AppConfig.getDatabasePassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getString("USER_ID"),
                        rs.getString("NAME"),
                        rs.getString("EMAIL"),
                        rs.getString("PASSWORD"),
                        rs.getString("ROLE"),
                        rs.getInt("LIMIT_BORROW_MAX"),
                        rs.getDate("DATE_REGISTRATION")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
        }
        return users;
    }

    /**
     * Deletes a user from the database by their user ID.
     *
     * @param userId The ID of the user to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public static boolean deleteUser(String userId) {
        String sql = "DELETE FROM USERS WHERE USER_ID = ?";

        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), 
                                                           AppConfig.getDatabaseUser(), 
                                                           AppConfig.getDatabasePassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hashes a password using SHA-256.
     *
     * @param password The plain-text password.
     * @return A hashed hexadecimal representation of the password.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            String hashedPassword = sb.toString();
            System.out.println("🔐 Hashed Password: " + hashedPassword);
            return hashedPassword;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Toggles the role of a user between 'USER' and 'ADMIN'.
     *
     * @param userId The ID of the user whose role should be toggled.
     * @return true if update was successful, false otherwise.
     */
    public static boolean changeUserRole(String userId) {
        String sql = "UPDATE USERS SET ROLE = CASE WHEN ROLE = 'USER' THEN 'ADMIN' ELSE 'USER' END WHERE USER_ID = ?";

        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), 
                                                           AppConfig.getDatabaseUser(), 
                                                           AppConfig.getDatabasePassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a default admin user if no users exist in the database.
     */
    public static void createAdminIfNotExists() {
        String checkSql = "SELECT COUNT(*) FROM USERS";
        String insertSql = "INSERT INTO USERS (USER_ID, NAME, EMAIL, PASSWORD, ROLE, LIMIT_BORROW_MAX, DATE_REGISTRATION) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), 
                                                           AppConfig.getDatabaseUser(), 
                                                           AppConfig.getDatabasePassword());
             Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, AppConfig.getAdminId());
                    insertStmt.setString(2, AppConfig.getAdminName());
                    insertStmt.setString(3, AppConfig.getAdminEmail());
                    insertStmt.setString(4, hashPassword(AppConfig.getAdminPassword()));
                    insertStmt.setString(5, AppConfig.getAdminRole());
                    insertStmt.setInt(6, AppConfig.getAdminLimitBorrowMax());
                    insertStmt.setDate(7, new java.sql.Date(System.currentTimeMillis()));

                    insertStmt.executeUpdate();
                    System.out.println("✅ First admin user created!");
                }
            } else {
                System.out.println("ℹ️ Users already exist, skipping admin creation.");
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
        }
    }
}
