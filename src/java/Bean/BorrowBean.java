package Bean;

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

@Named
@SessionScoped
public class BorrowBean implements Serializable {

    private List<Borrowing> currentBorrowedByUser;
    private Movie selectedMovie;
    private Date borrowDate;
        private int availableCopiesAfterBorrow;
private int remainingBorrowsAfterBorrow;

    @Inject
    private MovieBean movieBean;
    @Inject
    private UserBean userBean;

    @Inject
    private PageController pageController;

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

    boolean success = BorrowingDAO.addBorrowing(borrowing);
    if (success) {
        // עדכון מלאי
        movie.setCopiesAvailable(movie.getCopiesAvailable() - 1);
        MovieDAO.updateMovieInventory(movie);

        // הגדרת סרט נבחר והיסטוריית השאלה
        this.selectedMovie = movie;
        this.borrowDate = borrowing.getBorrowDate();

        // עדכון הסרט ב־movieBean אם הוא מוצג כרגע
        if (movieBean.getSelectedMovie() != null &&
            movieBean.getSelectedMovie().getMovieId().equals(movie.getMovieId())) {
            movieBean.getSelectedMovie().setCopiesAvailable(movie.getCopiesAvailable());
        }

        // רענון מלאי עבור עמודי ניהול
        movieBean.loadMovies();

        // חישוב משתנים לעמוד Borrow Confirmation
        this.availableCopiesAfterBorrow = movie.getCopiesAvailable();
        this.remainingBorrowsAfterBorrow = user.getLimitBorrowMax() - (active.size() + 1);

        refreshBorrowingLists();
        pageController.setPage("borrowConfirmation");
    } else {
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to borrow."));
    }
}


    public List<Borrowing> getCurrentBorrowedByUser() {
        if (currentBorrowedByUser == null) {
            refreshBorrowingLists();
        }
        return currentBorrowedByUser;
    }

    public int getRemainingBorrowLimit() {
        if (userBean.getLoggedInUser() == null) {
            return 0;
        }

        // נטעין כל פעם מהמסד
        List<Borrowing> active = BorrowingDAO.getCurrentBorrowedByUser(userBean.getLoggedInUser().getUserId());

        int maxAllowed = userBean.getLoggedInUser().getLimitBorrowMax();
        int current = active.size();
        int remaining = maxAllowed - current;

        System.out.println("🧮 Borrow Limit: Max=" + maxAllowed + ", Active=" + current + ", Remaining=" + remaining);

        return Math.max(0, remaining);
    }

    public int getTotalBorrowingsByUser() {
        if (userBean.getLoggedInUser() == null) {
            return 0;
        }
        List<Borrowing> history = BorrowingDAO.getUserBorrowingHistory(userBean.getLoggedInUser().getUserId());
        return history.size();
    }

    private void refreshBorrowingLists() {
        if (userBean.getLoggedInUser() != null) {
            this.currentBorrowedByUser = BorrowingDAO.getCurrentBorrowedByUser(userBean.getLoggedInUser().getUserId());
        }
    }

    public boolean canUserBorrow() {
        if (userBean.getLoggedInUser() == null) {
            return false;
        }
        return getRemainingBorrowLimit() > 0;
    }

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

                    // קריאה לפונקציה שמחזירה עותק למלאי
                    movieBean.returnCopyToInventory(movieId);

                    // הדפסה לאחר העדכון (נבדוק את העותקים העדכניים)
                    Movie movie = movieBean.getMovieByIdFromList(movieId); // פונקציה שמחזירה סרט מתוך הרשימה שב־movieBean
                    if (movie != null) {
                        System.out.println("🎥 Movie: " + movie.getTitle() + " | Available copies after return: " + movie.getCopiesAvailable());
                    } else {
                        System.out.println("⚠️ Could not find movie in list after return.");
                    }

                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Movie returned successfully."));
                    refreshBorrowingLists();
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

}
