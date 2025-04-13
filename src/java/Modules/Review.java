package Modules;

import java.sql.Timestamp;

/**
 * Represents a review given by a user for a movie.
 */
public class Review {

    private String reviewId;
    private String userId;
    private String movieId;
    private String comment;
    private int rating;
    private Timestamp dateReview;

    /**
     * Default constructor.
     */
    public Review() {
    }

    /**
     * Constructs a review with all fields.
     *
     * @param reviewId   the ID of the review
     * @param userId     the ID of the user who submitted the review
     * @param movieId    the ID of the movie being reviewed
     * @param comment    the review text
     * @param rating     the rating given (1–5)
     * @param dateReview the date and time the review was created
     */
    public Review(String reviewId, String userId, String movieId, String comment, int rating, Timestamp dateReview) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.movieId = movieId;
        this.comment = comment;
        this.rating = rating;
        this.dateReview = dateReview;
    }

    /**
     * Returns the ID of the review.
     *
     * @return the review ID
     */
    public String getReviewId() {
        return reviewId;
    }

    /**
     * Sets the ID of the review.
     *
     * @param reviewId the review ID to set
     */
    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    /**
     * Returns the ID of the user who submitted the review.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who submitted the review.
     *
     * @param userId the user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the ID of the movie being reviewed.
     *
     * @return the movie ID
     */
    public String getMovieId() {
        return movieId;
    }

    /**
     * Sets the ID of the movie being reviewed.
     *
     * @param movieId the movie ID to set
     */
    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    /**
     * Returns the text of the review.
     *
     * @return the comment text
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the text of the review.
     *
     * @param comment the comment text to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the rating given in the review.
     *
     * @return the rating (1–5)
     */
    public int getRating() {
        return rating;
    }

    /**
     * Sets the rating given in the review.
     *
     * @param rating the rating to set (should be between 1 and 5)
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Returns the date and time when the review was submitted.
     *
     * @return the review timestamp
     */
    public Timestamp getDateReview() {
        return dateReview;
    }

    /**
     * Sets the date and time when the review was submitted.
     *
     * @param dateReview the review timestamp to set
     */
    public void setDateReview(Timestamp dateReview) {
        this.dateReview = dateReview;
    }
}
