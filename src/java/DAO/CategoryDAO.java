package DAO;

import Modules.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import config.AppConfig;

/**
 * Data Access Object (DAO) responsible for performing all database operations
 * related to categories in the system. This includes retrieving all categories,
 * adding new ones, checking for duplicates, deleting by ID, updating details,
 * and fetching associated poster images. 
 * All database interactions are performed using JDBC with proper resource management.
 */

public class CategoryDAO {

    /**
     * Retrieves all categories from the database.
     *
     * @return a list of Category objects
     */
    public static List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT CATEGORY_ID, NAME_CATEGORY, DESCRIPTION, POSTER_IMAGE FROM CATEGORIES";
        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Category category = new Category(
                        rs.getString("CATEGORY_ID"),
                        rs.getString("NAME_CATEGORY"),
                        rs.getString("DESCRIPTION")
                );
                category.setPosterImage(rs.getBytes("POSTER_IMAGE"));
                categories.add(category);
            }
        } catch (Exception e) {
            System.out.println("Error retrieving categories: " + e.getMessage());
        }
        return categories;
    }

    /**
     * Adds a new category to the database.
     *
     * @param category the Category object to add
     * @return true if the category was successfully added, false otherwise
     */
    public static boolean addCategory(Category category) {
        if (categoryExists(category.getName())) {
            System.out.println("Error: Category '" + category.getName() + "' already exists!");
            return false;
        }
        String sql = "INSERT INTO CATEGORIES (CATEGORY_ID, NAME_CATEGORY, DESCRIPTION, POSTER_IMAGE) VALUES (?, ?, ?, ?)";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getCategoryId());
            pstmt.setString(2, category.getName().trim());
            pstmt.setString(3, category.getDescription().trim());
            pstmt.setBytes(4, category.getPosterImage() != null ? category.getPosterImage() : new byte[0]);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding category: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a category with the given name already exists in the database.
     *
     * @param categoryName the name of the category to check
     * @return true if it exists, false otherwise
     */
    public static boolean categoryExists(String categoryName) {
        String sql = "SELECT COUNT(*) FROM CATEGORIES WHERE LOWER(TRIM(NAME_CATEGORY)) = LOWER(TRIM(?))";

        try (Connection conn = AppConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryName.trim());
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error checking category existence: " + e.getMessage());
        }

        return false;
    }

    /**
     * Deletes a category from the database by ID.
     *
     * @param categoryId the ID of the category to delete
     * @return true if deleted successfully, false otherwise
     */
    public static boolean deleteCategory(String categoryId) {
        String sql = "DELETE FROM CATEGORIES WHERE CATEGORY_ID = ?";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Deleted category: " + categoryId);
                return true;
            } else {
                System.out.println("No category found with ID: " + categoryId);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error while deleting category: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the poster image for a category by ID.
     *
     * @param categoryId the ID of the category
     * @return byte array of the image, or null if not found
     */
    public static byte[] getCategoryImage(String categoryId) {
        String sql = "SELECT POSTER_IMAGE FROM CATEGORIES WHERE CATEGORY_ID = ?";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("POSTER_IMAGE");            }
        } catch (SQLException e) {
            System.out.println("Error retrieving category image: " + e.getMessage());
        }

        return null;
    }

    /**
     * Updates the details of an existing category.
     *
     * @param category the updated Category object
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateCategory(Category category) {
        String sql = "UPDATE CATEGORIES SET NAME_CATEGORY=?, DESCRIPTION=?, POSTER_IMAGE=? WHERE CATEGORY_ID=?";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setBytes(3, category.getPosterImage());
            pstmt.setString(4, category.getCategoryId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating category: " + e.getMessage());
            return false;
        }
    }
}
