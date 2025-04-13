package Bean;

import Modules.Review;
import DAO.ReviewDAO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

/**
 * Managed Bean for handling user reviews in the JSF interface.
 */
@Named("reviewBean")
@RequestScoped
public class ReviewBean implements Serializable {

    private String userId;
    private String movieId;
    private String comment;
    private int rating;

    @Inject
    private UserBean userBean;

    @Inject
    private MovieBean movieBean;

    /**
     * Submits a new review based on the input fields.
     *
     * @return navigation outcome or null to stay on the same page
     */
    public String submitReview() {
        if (userBean == null || userBean.getLoggedInUser() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "You must be logged in to review."));
            return null;
        }

        userId = userBean.getLoggedInUser().getUserId();
        movieId = movieBean.getSelectedMovie().getMovieId();
        System.out.println("📥 Submitting review: userId=" + userId + ", movieId=" + movieId + ", rating=" + rating + ", comment=" + comment);
        if (comment == null || comment.trim().isEmpty() || rating == 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Rating and comment are required."));
            return null;
        }

        if (ReviewDAO.hasUserReviewed(userId, movieId)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "You already reviewed this movie."));
            return null;
        }

        String reviewId = UUID.randomUUID().toString();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Review review = new Review(reviewId, userId, movieId, comment, rating, now);
        boolean success = ReviewDAO.addReview(review);

        if (success) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Review submitted successfully!"));
            comment = "";
            rating = 0;
            this.movieId = movieId;
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to submit review."));
        }

        return null;
    }

    /**
     * Retrieves the list of reviews for the selected movie.
     *
     * @return list of Review objects
     */
    public List<Review> getReviewsForMovie() {
    if (movieId != null) {
        System.out.println("📋 Loading reviews for movieId=" + movieId); // הדפסה נוספת כדי לבדוק את ה־movieId
        return ReviewDAO.getReviewsByMovie(movieId);
    }
    System.out.println("❌ movieId is null. No reviews to load.");
    return null;
}


    /**
     * Retrieves the average rating for the selected movie.
     *
     * @return the average rating as a double
     */
    public double getAverageRatingForMovie() {
        if (movieId != null) {
            return ReviewDAO.getAverageRating(movieId);
        }
        return 0.0;
    }

    // Getters and Setters
    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId the user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the movie ID.
     *
     * @return the movie ID
     */
    public String getMovieId() {
        return movieId;
    }

    /**
     * Sets the movie ID.
     *
     * @param movieId the movie ID to set
     */
    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    /**
     * Gets the comment text.
     *
     * @return the comment text
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment text.
     *
     * @param comment the comment text to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the rating value.
     *
     * @return the rating value
     */
    public int getRating() {
        return rating;
    }

    /**
     * Sets the rating value.
     *
     * @param rating the rating value to set (1–5)
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<Boolean> getStarList(double rating) {
        int fullStars = (int) Math.floor(rating);
        List<Boolean> stars = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            stars.add(i < fullStars); // true = full, false = empty
        }
        return stars;
    }

    public void deleteMyReview() {
        String userId = userBean.getLoggedInUser().getUserId();
        String movieId = movieBean.getSelectedMovie().getMovieId();

        System.out.println("🗑 Deleting own review: userId=" + userId + ", movieId=" + movieId);

        boolean success = ReviewDAO.deleteReview(userId, movieId);

        FacesContext context = FacesContext.getCurrentInstance();
        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Deleted", "Your review has been deleted."));
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete review."));
        }

        this.movieId = movieId;
    }

    public void deleteReviewByAdmin(String reviewId) {
        FacesContext context = FacesContext.getCurrentInstance();

        if ("ADMIN".equals(userBean.getLoggedInUser().getRole())) {
            System.out.println("🛠 Admin deleting review: reviewId=" + reviewId);

            boolean success = ReviewDAO.deleteReviewById(reviewId);

            if (success) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Deleted", "Review deleted successfully."));
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete review."));
            }

            this.movieId = movieBean.getSelectedMovie().getMovieId(); // refresh average
        }
    }

}
