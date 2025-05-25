package Bean;

import Utils.MathUtils;
import Utils.PageController;
import Modules.Borrowing;
import Modules.User;
import Modules.Movie;
import DAO.BorrowingDAO;
import DAO.MovieDAO;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import config.AppConfig;

/**
 * Managed Bean responsible for handling movie borrowing logic in the system.
 * Includes borrowing, returning, loss reporting, and credit card validation.
 * Session-scoped and tied to the current logged-in user.
 */
@Named
@SessionScoped
public class BorrowBean implements Serializable {

    private List<Borrowing> currentBorrowedByUser;// Currently borrowed movies by the user (not yet returned)
    private List<Borrowing> borrowingHistoryByUser;// Full borrowing history of the user
    private Movie selectedMovie;// Movie that was most recently borrowed
    private Date borrowDate;// Date of the latest borrow action
    private int availableCopiesAfterBorrow;// Remaining copies of the movie after borrowing
    private int remainingBorrowsAfterBorrow;// Remaining borrow slots after the current borrow
    private String pendingLossMovieId;// Movie ID for which the user is submitting a loss report
    private String creditCardNumber;// Credit card number entered for loss report
    private String cvc;// Credit card CVC code
    private String expiryMonth;// Credit card expiry month
    private String expiryYear;// Credit card expiry year
    @Inject
    private MovieBean movieBean;// Injected reference to movie-related logic
    @Inject
    private UserBean userBean;// Injected reference to the currently logged-in user
    @Inject
    private PageController pageController;// Controller for navigating between pages

    // === Getters and Setters ===
    public Movie getSelectedMovie() {
        return selectedMovie;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public int getAvailableCopiesAfterBorrow() {
        return availableCopiesAfterBorrow;
    }

    public int getRemainingBorrowsAfterBorrow() {
        return remainingBorrowsAfterBorrow;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public List<Borrowing> getBorrowingHistoryByUser() {
        if (borrowingHistoryByUser == null) {
            loadBorrowingHistory();
        }
        return borrowingHistoryByUser;
    }

    public List<Borrowing> getCurrentBorrowedByUser() {
        refreshBorrowingLists();

        return currentBorrowedByUser;
    }

    /**
     * Calculates the number of remaining movies the logged-in user is allowed
     * to borrow.
     *
     * @return the number of remaining borrow slots; returns 0 if the user is
     * not logged in.
     */
    public int getRemainingBorrowLimit() {
        if (userBean.getLoggedInUser() == null) {
            return 0;
        }

        List<Borrowing> active = BorrowingDAO.getCurrentBorrowedByUser(userBean.getLoggedInUser().getUserId());

        int maxAllowed = userBean.getLoggedInUser().getLimitBorrowMax();
        int current = active.size();
        int remaining = maxAllowed - current;
        return MathUtils.ensureNonNegative(remaining);
    }

    /**
     * Retrieves the total number of borrowings ever made by the logged-in user.
     *
     * @return the total count of borrowings; returns 0 if the user is not
     * logged in.
     */
    public int getTotalBorrowingsByUser() {
        if (userBean.getLoggedInUser() == null) {
            return 0;
        }
        List<Borrowing> history = BorrowingDAO.getUserBorrowingHistory(userBean.getLoggedInUser().getUserId());
        return history.size();
    }

    /**
     * Retrieves the list of borrowings that are marked as pending loss and are
     * awaiting admin approval.
     *
     * @return a list of pending loss borrowings
     */
    public List<Borrowing> getPendingLosses() {
        return BorrowingDAO.getPendingLosses();
    }

    /**
     * Attempts to borrow a movie for the logged-in user based on the provided
     * movie ID. Performs validation for login, movie availability, borrow
     * limits, and duplicate borrow prevention. Updates inventory and redirects
     * to the confirmation page upon success.
     *
     * @param movieId the ID of the movie to borrow
     */
    public void borrowMovie(String movieId) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (userBean == null || userBean.getLoggedInUser() == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You must be logged in."));
            return;
        }
        User user = userBean.getLoggedInUser();
        List<Movie> allMovies = MovieDAO.getAllMovies();
        Movie movie = allMovies.stream()
                .filter(m -> m.getMovieId().equals(movieId))
                .findFirst()
                .orElse(null);

        if (movie == null || movie.getCopiesAvailable() <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Movie is not available."));
            return;
        }

        List<Borrowing> active = BorrowingDAO.getCurrentBorrowedByUser(user.getUserId());
        if (active.size() >= user.getLimitBorrowMax()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Borrow limit reached."));
            return;
        }

        Borrowing borrowing = new Borrowing(
                UUID.randomUUID().toString(),
                user.getUserId(),
                movie.getMovieId(),
                new Date(),
                null
        );
        //  Check if the user already borrowed this movie
        boolean alreadyBorrowed = active.stream()
                .anyMatch(b -> b.getMovieId().equals(movieId));
        if (alreadyBorrowed) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Already Borrowed", "You already borrowed this movie."));
            return;
        }
        boolean success = BorrowingDAO.addBorrowing(borrowing);
        if (success) {
            // Update inventory
            movie.setCopiesAvailable(movie.getCopiesAvailable() - 1);
            MovieDAO.updateMovieInventory(movie);
            // Set selected movie and borrow date
            this.selectedMovie = movie;
            this.borrowDate = borrowing.getBorrowDate();
            // Update selected movie in movieBean if currently viewed
            if (movieBean.getSelectedMovie() != null
                    && movieBean.getSelectedMovie().getMovieId().equals(movie.getMovieId())) {
                movieBean.getSelectedMovie().setCopiesAvailable(movie.getCopiesAvailable());
            }
            // Refresh movie list for admin pages
            movieBean.loadMovies();
            // Calculate values for Borrow Confirmation page
            this.availableCopiesAfterBorrow = movie.getCopiesAvailable();
            int remaining = user.getLimitBorrowMax() - (active.size() + 1);
            this.remainingBorrowsAfterBorrow = MathUtils.ensureNonNegative(remaining);
            refreshBorrowingLists();
            pageController.setPage("borrowConfirmation");
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to borrow."));
        }
    }

    /**
     * Refreshes the list of currently borrowed movies by the logged-in user.
     * Retrieves the latest data from the database and stores a new copy
     * locally.
     */
    private void refreshBorrowingLists() {
        if (userBean.getLoggedInUser() != null) {
            List<Borrowing> freshList = BorrowingDAO.getCurrentBorrowedByUser(userBean.getLoggedInUser().getUserId());
            this.currentBorrowedByUser = new ArrayList<>(freshList); // יוצר עותק חדש
        }
    }

    /**
     * Checks whether the logged-in user is allowed to borrow more movies based
     * on their remaining borrow limit.
     *
     * @return true if the user can borrow more movies, false otherwise
     */
    public boolean canUserBorrow() {
        if (userBean.getLoggedInUser() == null) {
            return false;
        }
        return getRemainingBorrowLimit() > 0;
    }

    /**
     * Handles the return process of a borrowed movie. Marks the borrowing as
     * returned in the database, updates the movie inventory, refreshes the
     * borrowing lists, and displays appropriate UI messages.
     *
     * @param movieId the ID of the movie to return
     */
    public void returnMovie(String movieId) {
        User user = userBean.getLoggedInUser();
        if (user == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "User not logged in."));
            return;
        }
        for (Borrowing active : currentBorrowedByUser) {
            if (active.getMovieId().equals(movieId)) {
                java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
                boolean updated = BorrowingDAO.markAsReturned(active.getBorrowingId(), sqlDate);
                if (updated) {
                    System.out.println("✅ Borrowing marked as returned for borrowingId: " + active.getBorrowingId());
                    // Call the function that returns the copy back to inventory
                    movieBean.returnCopyToInventory(movieId);
                    // Print the movie after update (check updated available copies)
                    Movie movie = movieBean.getMovieByIdFromList(movieId); //Gets the movie from the list in movieBean
                    if (movie != null) {
                        System.out.println("🎥 Movie: " + movie.getTitle() + " | Available copies after return: " + movie.getCopiesAvailable());
                    } else {
                        System.out.println("⚠️ Could not find movie in list after return.");
                    }
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Movie returned successfully."));
                    refreshBorrowingLists();
                    loadBorrowingHistory();
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to return the movie."));
                }
                return;
            }
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Active borrowing not found."));
    }

    /**
     * Initializes a loss report request for the specified movie and navigates
     * to the report loss page.
     *
     * @param movieId the ID of the movie being reported as lost
     */
    public void initLossRequest(String movieId) {
        this.pendingLossMovieId = movieId;
        pageController.setPage("reportLoss");
    }

    /**
     * Validates the credit card details entered by the user. Checks that the
     * number is 16 digits, CVC is 3 digits, and the expiry date is in valid
     * format and not expired. Displays appropriate error messages via the
     * provided FacesContext.
     *
     * This method is marked as private because it is only used internally
     * within the BorrowBean class (specifically by submitLossRequest) and is
     * not intended to be accessed from JSF pages or other classes.
     *
     * @param context the JSF context used to display error messages
     * @return true if all credit card details are valid, false otherwise
     */
    private boolean validateCreditCardDetails(FacesContext context) {
        if (creditCardNumber == null || !creditCardNumber.matches("\\d{" + AppConfig.getCreditCardLength() + "}")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Credit card number must be 16 digits."));
            return false;
        }
        if (cvc == null || !cvc.matches("\\d{" + AppConfig.getCvcLength() + "}")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "CVC must be 3 digits."));
            return false;
        }
        if (expiryMonth == null || expiryYear == null
                || !expiryMonth.matches("\\d{1,2}") || !expiryYear.matches("\\d{4}")) {// Ensure expiry month and year are not null and have valid digit formats
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Invalid expiry date."));
            return false;
        }
        try {
            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);
            if (month < AppConfig.getMinMonth() || month > AppConfig.getMaxMonth()) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "Invalid month (1-12)."));
                return false;
            }
            java.time.YearMonth expiry = java.time.YearMonth.of(year, month);
            java.time.YearMonth now = java.time.YearMonth.now();
            if (expiry.isBefore(now)) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "Card has expired."));
                return false;
            }
        } catch (NumberFormatException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Invalid expiry date format."));
            return false;
        }
        return true;
    }

    /**
     * Clears all credit card input fields. This method is used after a
     * successful loss request submission to reset the form and prevent data
     * retention in the session scope.
     */
    private void clearCreditCardFields() {
        this.creditCardNumber = "";
        this.cvc = "";
        this.expiryMonth = "";
        this.expiryYear = "";
    }

    /**
     * Submits a loss report request for the currently selected movie. Validates
     * credit card details, updates the borrowing status in the database, clears
     * input fields, updates UI messages, and redirects the user to their
     * profile.
     */
    public void submitLossRequest() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (!validateCreditCardDetails(context)) {
            return;
        }
        String userId = userBean.getLoggedInUser().getUserId();
        String movieId = pendingLossMovieId;
        boolean success = BorrowingDAO.markLossPending(userId, movieId);
        if (success) {
            refreshBorrowingLists();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Submitted", "Loss report submitted. Pending admin approval."));
            pageController.setPage("profile");
            clearCreditCardFields();
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Failed to submit loss request."));
        }
    }

    /**
     * Confirms a pending loss report by its borrowing ID. Updates the borrowing
     * status in the database, attempts to reduce the total number of copies of
     * the associated movie, and refreshes movie and borrowing data. Displays
     * appropriate messages depending on the outcome.
     *
     * @param borrowingId the ID of the borrowing to confirm as lost
     */
    public void confirmLoss(String borrowingId) {
        FacesContext context = FacesContext.getCurrentInstance();

        boolean success = BorrowingDAO.confirmLossAndUpdate(borrowingId);

        if (success) {
            Borrowing borrowing = BorrowingDAO.getBorrowingById(borrowingId);
            if (borrowing != null) {
                // Update the total number of copies for the movie
                boolean quantityReduced = MovieDAO.decreaseTotalCopies(borrowing.getMovieId());
                if (!quantityReduced) {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Warning", "Loss confirmed, but total copies not reduced."));
                }
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Loss confirmed", "Could not fetch borrowing details to update inventory and limit."));
            }
            // Refresh movie and borrowing lists
            movieBean.loadMovies();
            refreshBorrowingLists();
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Failed to confirm the loss."));
        }
    }

    /**
     * Loads the full borrowing history of the logged-in user from the database
     * and stores it in the local field. Called internally to support UI
     * rendering.
     *
     * This method is private because it is only used inside BorrowBean and is
     * not intended to be accessed directly from other classes or JSF views.
     */
    private void loadBorrowingHistory() {
        if (userBean != null && userBean.getLoggedInUser() != null) {
            this.borrowingHistoryByUser = BorrowingDAO.getAllBorrowingsByUser(userBean.getLoggedInUser().getUserId());
        }
    }

}
