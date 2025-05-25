package Bean;

import Modules.Review;
import DAO.ReviewDAO;
import DAO.UserDAO;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import java.util.Map;
import Utils.PageController;
import Utils.MathUtils;
import config.AppConfig;

/**
 * ReviewBean is a JSF managed bean that handles user review operations within
 * the application. It allows users to submit, view, and delete reviews for
 * movies, and allows admins to manage reviews. The bean manages UI interactions
 * such as displaying average ratings, star visuals, and error messages.
 */
@Named("reviewBean")
@SessionScoped
public class ReviewBean implements Serializable {

    private String userId; // ID of the user submitting or managing the review
    private String movieId; // ID of the movie being reviewed
    private String comment; // The review comment text
    private int rating; // The numeric rating given by the user (e.g., 1–5)
    private List<Review> reviewsForMovie = new ArrayList<>(); // List of all reviews associated with the selected movie
    @Inject
    private UserBean userBean; // Injected reference to the logged-in user context
    @Inject
    private MovieBean movieBean; // Injected reference to the movie context (e.g., selected movie)

    /**
     * Handles the submission of a new movie review. Validates input fields,
     * prevents duplicate reviews, and updates the movie list if successful.
     *
     * @return null to remain on the same page after processing
     */
    public String submitReview() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (userBean == null || userBean.getLoggedInUser() == null) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Login Required",
                            "You must be logged in to submit a review."));
            return null;
        }
        userId = userBean.getLoggedInUser().getUserId();
        movieId = movieBean.getSelectedMovie().getMovieId();
        System.out.println("📥 Submitting review: userId=" + userId + ", movieId=" + movieId + ", rating=" + rating + ", comment=" + comment);
        if (comment == null || comment.trim().isEmpty() || rating == AppConfig.getInvalidRating()) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Missing Fields",
                            "Please provide both a rating and a comment before submitting."));
            return null;
        }
        if (ReviewDAO.hasUserReviewed(userId, movieId)) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Review Already Exists",
                            "You have already submitted a review for this movie. Only one review is allowed."));
            return null;
        }
        String reviewId = UUID.randomUUID().toString();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Review review = new Review(reviewId, userId, movieId, comment, rating, now);
        boolean success = ReviewDAO.addReview(review);
        if (success) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Review Submitted",
                            "Thank you! Your review was submitted successfully."));
            comment = "";
            rating = 0;
            movieBean.loadMovies();
        } else {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Submission Failed",
                            "An error occurred while submitting your review. Please try again."));
        }
        return null;
    }

    /**
     * Retrieves the list of reviews for the selected movie.
     *
     * @return list of Review objects
     */
    public List<Review> getReviewsForMovie() {
        return reviewsForMovie;
    }

    /**
     * Retrieves the average rating for the currently selected movie.
     *
     * @return the average rating value, or default value if no movie is
     * selected
     */
    public double getAverageRatingForMovie() {
        if (movieId != null) {
            return ReviewDAO.getAverageRating(movieId);
        }
        return AppConfig.getDefaultAverageRating();
    }

    public double getAverageRatingForMovie(String movieId) {
        if (movieId != null) {
            return ReviewDAO.getAverageRating(movieId);
        }
        return AppConfig.getDefaultAverageRating();
    }

    // Getters and Setters   
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
        loadReviewsForMovie();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUserNameById(String userId) {
        return UserDAO.getUserNameById(userId);
    }

    /**
     * Converts a numeric rating into a list of booleans representing full
     * stars.
     *
     * @param rating the rating value
     * @return list of 5 boolean values, true for full star, false for empty
     */
    public List<Boolean> getStarList(double rating) {
        return MathUtils.getStarBooleans(rating, AppConfig.getMaxStars());
    }

    /**
     * Deletes a review by its ID, only if the logged-in user is an admin.
     * Reloads the movie and its reviews if the deletion succeeds.
     *
     * @param reviewId the ID of the review to delete
     */
    public void deleteReviewByAdmin(String reviewId) {
        System.out.println("Attempting to delete review...");
        if (!"ADMIN".equals(userBean.getLoggedInUser().getRole())) {
            System.out.println("️ [deleteReviewByAdmin] User is not admin. Aborting.");
            return;
        }
        System.out.println(" [deleteReviewByAdmin] Called for reviewId=" + reviewId);
        boolean success = ReviewDAO.deleteReviewById(reviewId);
        FacesContext context = FacesContext.getCurrentInstance();
        if (success) {
            System.out.println(" [deleteReviewByAdmin] Review deleted.");
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Deleted", "Review deleted successfully."));
            this.movieId = movieBean.getSelectedMovie().getMovieId();
            loadReviewsForMovie();
            movieBean.loadMovies();
        } else {
            System.out.println(" [deleteReviewByAdmin] Failed to delete review.");
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete review."));
        }
        this.movieId = movieBean.getSelectedMovie().getMovieId();
    }

    /**
     * Loads all reviews for the currently selected movie into the local list.
     * If no movie is selected, the list is cleared.
     */
    public void loadReviewsForMovie() {
        if (movieId != null) {
            reviewsForMovie = ReviewDAO.getReviewsByMovie(movieId);
        } else {
            reviewsForMovie.clear();
        }
    }

    /**
     * Deletes a review by its ID if the user is logged in. Displays a success
     * or error message and reloads the review list if needed.
     *
     * @param reviewId the ID of the review to delete
     */
    public void deleteReviewById(String reviewId) {
        System.out.println(" Attempting to delete review by ID...");
        FacesContext context = FacesContext.getCurrentInstance();
        if (userBean.getLoggedInUser() == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "You must be logged in."));
            return;
        }
        boolean success = ReviewDAO.deleteReviewById(reviewId);
        if (success) {
            System.out.println(" Review deleted.");
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Deleted", "Review deleted successfully."));
            loadReviewsForMovie();
        } else {
            System.out.println(" Failed to delete review.");
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete review."));
        }
    }

    /**
     * Loads reviews for a movie based on the "movieId" request parameter. If
     * found, sets the movie ID, loads its reviews, and navigates to the reviews
     * page.
     */
    public void loadSelectedMovieReviews() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String movieIdParam = params.get("movieId");
        if (movieIdParam != null && !movieIdParam.isEmpty()) {
            this.movieId = movieIdParam;
            System.out.println(" Loading reviews for movieId = " + movieId);
            loadReviewsForMovie();
            context.getApplication()
                    .evaluateExpressionGet(context, "#{pageController}", PageController.class)
                    .setPage("viewReviews");
        } else {
            System.out.println(" No movieId parameter found to load reviews.");
        }
    }
}
