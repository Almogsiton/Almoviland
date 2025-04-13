package DAO;

import Modules.Review;
import config.AppConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) class for performing operations on movie reviews.
 */
public class ReviewDAO {

    /**
     * Adds a new review to the database.
     *
     * @param review the Review object to be added
     * @return true if the review was added successfully, false otherwise
     */
    public static boolean addReview(Review review) {
        String sql = "INSERT INTO REVIEWS (REVIEW_ID, ID_USER, ID_MOVIE, COMMENT, RATING, DATE_REVIEW) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, review.getReviewId());
            pstmt.setString(2, review.getUserId());
            pstmt.setString(3, review.getMovieId());
            pstmt.setString(4, review.getComment());
            pstmt.setInt(5, review.getRating());
            pstmt.setTimestamp(6, review.getDateReview());

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Review added successfully.");
            } else {
                System.out.println("⚠️ No rows affected while inserting review.");
            }

            return success;

        } catch (SQLException e) {
            System.out.println("❌ SQLException - " + e.getMessage());
            System.out.println("📌 reviewId=" + review.getReviewId());
            System.out.println("📌 userId=" + review.getUserId());
            System.out.println("📌 movieId=" + review.getMovieId());
            System.out.println("📌 comment=" + review.getComment());
            System.out.println("📌 rating=" + review.getRating());
            System.out.println("📌 date=" + review.getDateReview());

            return false;
        }
    }

    /**
     * Retrieves a list of reviews for a specific movie.
     *
     * @param movieId the ID of the movie
     * @return a list of Review objects associated with the movie
     */
    public static List<Review> getReviewsByMovie(String movieId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM REVIEWS WHERE ID_MOVIE = ? ORDER BY DATE_REVIEW DESC";

        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reviews.add(new Review(
                        rs.getString("REVIEW_ID"),
                        rs.getString("ID_USER"),
                        rs.getString("ID_MOVIE"),
                        rs.getString("COMMENT"),
                        rs.getInt("RATING"),
                        rs.getTimestamp("DATE_REVIEW")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Failed to retrieve reviews: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Calculates the average rating for a specific movie.
     *
     * @param movieId the ID of the movie
     * @return the average rating as a double, or 0.0 if no ratings found
     */
    public static double getAverageRating(String movieId) {
        String sql = "SELECT AVG(RATING) AS AVG_RATING FROM REVIEWS WHERE ID_MOVIE = ?";

        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("AVG_RATING");
            }

        } catch (SQLException e) {
            System.out.println("Failed to calculate average rating: " + e.getMessage());
        }

        return 0.0;
    }

    // האם למשתמש כבר יש ביקורת לסרט?
    public static boolean hasUserReviewed(String userId, String movieId) {
    String sql = "SELECT COUNT(*) FROM REVIEWS WHERE ID_USER = ? AND ID_MOVIE = ?";
    System.out.println("🔎 Checking if user " + userId + " reviewed movie " + movieId);

    try (Connection conn = DriverManager.getConnection(
             AppConfig.getDatabaseUrl(),
             AppConfig.getDatabaseUser(),
             AppConfig.getDatabasePassword()); 
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, userId);
        stmt.setString(2, movieId);
        System.out.println("📝 Executing query: " + sql);
        
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int count = rs.getInt(1);
            System.out.println("🔢 Found " + count + " reviews for user " + userId + " and movie " + movieId);
            return count > 0;
        } else {
            System.out.println("🔍 No results found for user " + userId + " and movie " + movieId);
            return false;
        }

    } catch (SQLException e) {
        System.err.println("❌ SQL Error (hasUserReviewed): " + e.getMessage());
        return false;
    }
}


// מחיקת ביקורת של משתמש
    public static boolean deleteReview(String userId, String movieId) {
        String sql = "DELETE FROM REVIEWS WHERE ID_USER = ? AND ID_MOVIE = ?";
        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), AppConfig.getDatabaseUser(), AppConfig.getDatabasePassword()); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, movieId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ SQL Error (deleteReview): " + e.getMessage());
            return false;
        }
    }

// מחיקת ביקורת לפי ID - למנהל
    public static boolean deleteReviewById(String reviewId) {
        String sql = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), AppConfig.getDatabaseUser(), AppConfig.getDatabasePassword()); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reviewId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ SQL Error (deleteReviewById): " + e.getMessage());
            return false;
        }
    }

}
