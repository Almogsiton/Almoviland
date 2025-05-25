package DAO;

import Modules.Review;
import config.AppConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Utils.MathUtils;

/**
 * Data Access Object (DAO) class for performing CRUD operations on movie
 * reviews. This class provides methods to add, retrieve, update, and delete
 * reviews, as well as calculate average ratings for movies. It communicates
 * with the database using JDBC and maps review records to Review model objects.
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
        try (Connection conn = AppConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = AppConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        String sql = "SELECT RATING FROM REVIEWS WHERE ID_MOVIE = ?";
        int sum = 0;
        int count = 0;
        try (Connection conn = AppConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int rating = rs.getInt("RATING");
                sum += rating;
                count++;
            }
            if (count == 0) {
                return AppConfig.getDefaultAverageRating();
            }
            double avg = (double) sum / count;
            return MathUtils.roundTo1Decimal(avg);
        } catch (SQLException e) {
            System.out.println("❌ Failed to calculate average rating manually: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Checks whether a specific user has already submitted a review for a given
     * movie. This method queries the REVIEWS table to count the number of
     * existing reviews by the given user for the specified movie. It returns
     * true if at least one review exists, false otherwise.
     * @param userId the ID of the user
     * @param movieId the ID of the movie
     * @return true if the user has already reviewed the movie; false otherwise
     */
    public static boolean hasUserReviewed(String userId, String movieId) {
        String sql = "SELECT COUNT(*) FROM REVIEWS WHERE ID_USER = ? AND ID_MOVIE = ?";
        System.out.println("🔎 Checking if user " + userId + " reviewed movie " + movieId);
        try (Connection conn = AppConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    /**
     * Deletes a specific review submitted by a user for a given movie. This
     * method removes a review from the REVIEWS table based on the user ID and
     * movie ID. If a review exists for the given user and movie, it will be
     * deleted.
     *
     * @param userId the ID of the user who submitted the review
     * @param movieId the ID of the movie being reviewed
     * @return true if the review was successfully deleted; false if no matching
     * review was found or an error occurred
     */
    public static boolean deleteReview(String userId, String movieId) {
        String sql = "DELETE FROM REVIEWS WHERE ID_USER = ? AND ID_MOVIE = ?";
        System.out.println("Deleting review for user [" + userId + "] and movie [" + movieId + "]");
        try (Connection conn = AppConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, movieId);
            int affected = stmt.executeUpdate();
            System.out.println(" Rows affected: " + affected);
            return affected > 0;
        } catch (SQLException e) {
            System.err.println(" SQL Error (deleteReview): " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a review from the database based on its unique review ID. This
     * method executes a DELETE statement on the REVIEWS table to remove the
     * review with the specified ID.
     *
     * @param reviewId the unique identifier of the review to delete
     * @return true if the review was successfully deleted; false if not found
     * or an error occurred
     */
    public static boolean deleteReviewById(String reviewId) {
        String sql = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
        System.out.println("Deleting review with ID: " + reviewId);
        try (Connection conn = AppConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reviewId);
            int affected = stmt.executeUpdate();
            System.out.println(" Rows affected: " + affected);
            return affected > 0;
        } catch (SQLException e) {
            System.err.println(" SQL Error (deleteReviewById): " + e.getMessage());
            return false;
        }
    }
}
