package Modules;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a borrowing transaction in the system.
 *
 * This class stores information about a user's movie borrowing, including the
 * user ID, movie ID, borrowing date, and return date.
 */
public class Borrowing implements Serializable {

    private String borrowingId;
    private String userId;
    private String movieId;
    private Date borrowDate;
    private Date returnDate;
    private String status;

    /**
     * Constructs a new Borrowing object with the specified details.
     *
     * @param borrowingId the unique ID of the borrowing
     * @param userId the ID of the user who borrowed the movie
     * @param movieId the ID of the borrowed movie
     * @param borrowDate the date the movie was borrowed
     * @param returnDate the date the movie was returned
     */
    public Borrowing(String borrowingId, String userId, String movieId, Date borrowDate, Date returnDate) {
        this.borrowingId = borrowingId;
        this.userId = userId;
        this.movieId = movieId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    /**
     * Constructs an empty Borrowing object. Useful for frameworks or
     * serialization.
     */
    public Borrowing() {
    }

    /**
     * @return the borrowing ID
     */
    public String getBorrowingId() {
        return borrowingId;
    }

    /**
     * Sets the borrowing ID.
     *
     * @param borrowingId the borrowing ID to set
     */
    public void setBorrowingId(String borrowingId) {
        this.borrowingId = borrowingId;
    }

    /**
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
     * @return the borrowing date
     */
    public Date getBorrowDate() {
        return borrowDate;
    }

    /**
     * Sets the borrowing date.
     *
     * @param borrowDate the borrowing date to set
     */
    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }

    /**
     * @return the return date
     */
    public Date getReturnDate() {
        return returnDate;
    }

    /**
     * Sets the return date.
     *
     * @param returnDate the return date to set
     */
    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    /**
     * @return the borrowing status (e.g., "ACTIVE", "RETURNED", "LOST")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the borrowing status.
     *
     * @param status the status to set (e.g., "ACTIVE", "RETURNED", "LOST")
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
