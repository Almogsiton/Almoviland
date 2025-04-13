package Bean;

import Utils.PageController;
import Modules.Movie;
import Modules.Category;
import DAO.ReviewDAO;
import DAO.BorrowingDAO;
import DAO.CategoryDAO;
import DAO.MovieDAO;
import Utils.DateUtils;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Part;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import config.AppConfig;
import jakarta.inject.Inject;
import java.util.HashMap;
import jakarta.enterprise.context.SessionScoped;
import jakarta.annotation.PostConstruct;

/**
 * Managed Bean responsible for managing movies in the system. This bean handles
 * movie creation, deletion, updates, inventory management, and image handling.
 * It is session-scoped and tied to the user's session.
 */
@Named
@SessionScoped

public class MovieBean implements Serializable {

    private List<Movie> allMovies;
    private List<Category> allCategories;
    private Movie newMovie;
    private List<String> selectedCategories;
    private Part uploadedFile;
    private String minDate;
    private String maxDate;
    private Movie selectedMovie;
    private int addCopiesCount;
    @Inject
    private PageController pageController;
    private Map<String, List<Movie>> moviesByCategory;
    private Map<String, Integer> categoryIndexes;
    private List<Movie> popularMovies;
    private int popularIndex = 0;
    private final int PAGE_SIZE = 5;
    private String searchQuery = "";
    private final List<Movie> searchResults = new ArrayList<>();
    private int lostCopiesCount;
    private final Map<String, String> imageCache = new HashMap<>();

    /**
     * Constructs a new MovieBean instance and initializes the movie list,
     * category list, and release date limits.
     */
    public MovieBean() {
    }

    @PostConstruct
    public void init() {
        newMovie = new Movie();
        selectedCategories = new ArrayList<>();
        moviesByCategory = new HashMap<>();
        categoryIndexes = new HashMap<>();
        popularMovies = new ArrayList<>();
        popularIndex = 0;
        loadMovies();
        loadCategories();
        setDateLimits();
        loadImages();
    }

    private void loadImages() {
        for (Movie movie : allMovies) {
            byte[] imageBytes = movie.getPosterImage();
            String movieId = movie.getMovieId();

            if (imageBytes != null && imageBytes.length > 0) {
                
                String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
                if(imageCache.isEmpty()){
                System.out.println(base64Image);
                }
                imageCache.put(movieId, base64Image);
            } else {
                System.out.println("No image found for movie: " + movieId + ", using default.");
                imageCache.put(movieId, "/resources/images/default.PNG");
            }
        }
    }

    /**
     * Loads the list of all categories from the database.
     */
    private void loadCategories() {
        allCategories = CategoryDAO.getAllCategories();
    }

    /**
     * Loads the list of all movies from the database.
     */
    public void loadMovies() {
        List<Movie> movies = MovieDAO.getAllMovies();
        this.allMovies = movies;

        movies.sort((m1, m2) -> {
            double r1 = ReviewDAO.getAverageRating(m1.getMovieId());
            double r2 = ReviewDAO.getAverageRating(m2.getMovieId());
            return Double.compare(r2, r1);
        });

        moviesByCategory.clear();
        categoryIndexes.clear();

        for (Movie movie : movies) {
            if (ReviewDAO.getAverageRating(movie.getMovieId()) >= 4.0) {
                popularMovies.add(movie);
            }

            String[] categories = movie.getCategoryNames().split(",");
            for (String cat : categories) {
                String trimmed = cat.trim();
                moviesByCategory.computeIfAbsent(trimmed, k -> new ArrayList<>()).add(movie);
            }
        }

        for (String category : moviesByCategory.keySet()) {
            categoryIndexes.put(category, 0);
        }

        if (!popularMovies.isEmpty()) {
            popularIndex = 0;
        }
    }

    /**
     * @return a list of all movies available in the system.
     */
    public List<Movie> getMovies() {
        return allMovies;
    }

    /**
     * @return a list of all available movie categories.
     */
    public List<Category> getCategories() {
        return allCategories;
    }

    /**
     * @return the new movie being prepared for addition.
     */
    /**
     * @return the list of selected category IDs for the new movie.
     */
    public List<String> getSelectedCategories() {
        return selectedCategories;
    }

    /**
     * Sets the selected category IDs for the new movie.
     *
     * @param selectedCategories list of selected category IDs
     */
    public void setSelectedCategories(List<String> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    /**
     * @return the minimum allowed date for selecting a movie release date.
     */
    public String getMinDate() {
        return minDate;
    }

    /**
     * @return the maximum allowed date for selecting a movie release date.
     */
    public String getMaxDate() {
        return maxDate;
    }

    /**
     * @return the uploaded image file (poster) for the movie.
     */
    public Part getUploadedFile() {
        return uploadedFile;
    }

    /**
     * Sets the uploaded image file (poster) for the movie.
     *
     * @param uploadedFile the uploaded image part
     */
    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    /**
     * @return the currently selected movie for editing or updating.
     */
    public Movie getSelectedMovie() {
        return selectedMovie;
    }

    /**
     * Sets the movie selected for editing or updating.
     *
     * @param selectedMovie the movie to select
     */
    public void setSelectedMovie(Movie selectedMovie) {
        this.selectedMovie = selectedMovie;
    }

    /**
     * @return the number of copies to be added to the movie's inventory.
     */
    public int getAddCopiesCount() {
        return addCopiesCount;
    }

    /**
     * Sets the number of copies to be added to the inventory.
     *
     * @param addCopiesCount the number of copies to add
     */
    public void setAddCopiesCount(int addCopiesCount) {
        this.addCopiesCount = addCopiesCount;
    }

    public int getLostCopiesCount() {
        return lostCopiesCount;
    }

    public void setLostCopiesCount(int lostCopiesCount) {
        this.lostCopiesCount = lostCopiesCount;
    }

    public Movie getNewMovie() {
        return newMovie;
    }

    public void setNewMovie(Movie newMovie) {
        this.newMovie = newMovie;
    }

    /**
     * Sets the minimum and maximum release date limits for the UI date picker.
     * - Minimum: 100 years ago - Maximum: 10 years from now
     */
private void setDateLimits() {
    this.minDate = DateUtils.getMinReleaseDate();
    this.maxDate = DateUtils.getMaxReleaseDate();
}
    /**
     * Adds a new movie to the system after validating required fields and
     * handling poster image upload. If the movie already exists, or validation
     * fails, an error message is shown.
     */
    public void addMovie() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (newMovie.getTitle() == null || newMovie.getTitle().trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Movie title is required!"));
            return;
        }

        if (selectedCategories == null || selectedCategories.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "At least one category must be selected!"));
            return;
        }

        if (newMovie.getDateRelease() == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Release date is required!"));
            return;
        }

        if (MovieDAO.movieExists(newMovie.getTitle())) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Movie already exists!"));
            return;
        }

        newMovie.setMovieId(UUID.randomUUID().toString());
        newMovie.setCategoryNames(String.join(", ", selectedCategories));

        if (uploadedFile != null && uploadedFile.getSize() > 0) {
            if (!uploadedFile.getContentType().startsWith("image/")) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Only image files are allowed!"));
                return;
            }

            if (uploadedFile.getSize() > AppConfig.getMaxImageUploadSize()) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "File size exceeds 2MB limit!"));
                return;
            }

            try (InputStream input = uploadedFile.getInputStream()) {
                newMovie.setPosterImage(input.readAllBytes());
            } catch (IOException e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to read image!"));
                return;
            }
        }

        boolean success = MovieDAO.addMovie(newMovie, selectedCategories);

        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Movie added successfully!"));
            loadMovies();
            newMovie = new Movie();
            selectedCategories.clear();
            uploadedFile = null;
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to add movie!"));
        }
    }

    /**
     * Deletes a movie and its category links from the database.
     *
     * @param movieId the ID of the movie to delete
     */
    public void deleteMovie(String movieId) {
        FacesContext context = FacesContext.getCurrentInstance();
        boolean success = MovieDAO.deleteMovie(movieId);

        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Movie deleted successfully!"));
            loadMovies();
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to delete movie!"));
        }
    }

    /**
     * Decreases the total quantity of the selected movie by 1 due to a customer
     * loss. If the resulting quantity is negative, an error is shown.
     */
    public void movieLostByCustomer() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (selectedMovie != null) {
            int newQuantity = selectedMovie.getQuantity() - 1;

            if (newQuantity < 0) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Total copies cannot be negative!"));
                return;
            }

            selectedMovie.setQuantity(newQuantity);
            boolean success = MovieDAO.updateMovieInventory(selectedMovie);

            if (success) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "One copy lost by customer."));
                loadMovies();
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to update movie inventory."));
            }
        }
    }

    /**
     * Decreases both the total quantity and available copies of the selected
     * movie by a specified number due to a company-side loss. If the requested
     * loss exceeds the total or available copies, the operation is aborted and
     * an error message is shown.
     *
     * @param lostCount the number of copies marked as lost
     */
    public void movieLostByCompany(int lostCount) {
        FacesContext context = FacesContext.getCurrentInstance();

        if (selectedMovie != null && lostCount > 0) {
            int currentQuantity = selectedMovie.getQuantity();
            int currentAvailable = selectedMovie.getCopiesAvailable();

            if (lostCount > currentQuantity) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Cannot lose more than total copies!"));
                return;
            }

            if (lostCount > currentAvailable) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Cannot lose copies currently borrowed by customers!"));
                return;
            }

            selectedMovie.setQuantity(currentQuantity - lostCount);
            selectedMovie.setCopiesAvailable(currentAvailable - lostCount);

            boolean success = MovieDAO.updateMovieInventory(selectedMovie);

            if (success) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", lostCount + " copies marked as lost by company."));
                loadMovies();
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to update movie inventory."));
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Invalid number of lost copies."));
        }
    }

    /**
     * Loads a selected movie from request parameters and sets it as the current
     * editable movie. Also navigates to the movie editing page if the movie is
     * found.
     */
    public void loadSelectedMovie() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
        String movieId = params.get("movieId");

        if (movieId != null) {
            for (Movie movie : allMovies) {
                if (movie.getMovieId().equals(movieId)) {
                    selectedMovie = movie;
                    System.out.println("✔ Loaded movie for edit: " + selectedMovie.getTitle());
                    pageController.setPage("editMovie");
                    return;
                }
            }
        }

        System.out.println("❌ Movie not found for editing!");
    }

    /**
     * Adds the specified number of copies to the selected movie's inventory.
     * The available copies are updated accordingly, but will not exceed the
     * total quantity.
     */
    public void addCopiesToInventory() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (selectedMovie != null && addCopiesCount > 0) {
            selectedMovie.setQuantity(selectedMovie.getQuantity() + addCopiesCount);

            int updatedAvailable = Math.min(
                    selectedMovie.getCopiesAvailable() + addCopiesCount,
                    selectedMovie.getQuantity()
            );
            selectedMovie.setCopiesAvailable(updatedAvailable);

            boolean success = MovieDAO.updateMovieInventory(selectedMovie);

            if (success) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", addCopiesCount + " copies added to inventory."));
                context.getExternalContext().getFlash().setKeepMessages(true);
                loadMovies();
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Failed to add copies to inventory."));
                context.getExternalContext().getFlash().setKeepMessages(true);
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Invalid number of copies."));
            context.getExternalContext().getFlash().setKeepMessages(true);
        }
    }

    /**
     * Retrieves and returns the base64-encoded poster image of a movie.
     *
     * @param movieId the ID of the movie
     * @return base64 image string if available, otherwise a default placeholder
     * image path
     */
    public String getMovieImageBase64(String movieId) {
        return imageCache.getOrDefault(movieId, "/resources/images/default.PNG");
    }

    /**
     * Returns a list of all category names that contain at least one movie.
     *
     * @return list of category names
     */
    public List<String> getCategoryNames() {
        return new ArrayList<>(moviesByCategory.keySet());
    }

public List<Movie> getVisibleMoviesForCategory(String category) {
    return moviesByCategory.getOrDefault(category, new ArrayList<>());
}


    public boolean hasNextPage(String category) {
        List<Movie> all = moviesByCategory.getOrDefault(category, new ArrayList<>());
        int index = categoryIndexes.getOrDefault(category, 0);
        return index + PAGE_SIZE < all.size();
    }

    public boolean hasPreviousPage(String category) {
        return categoryIndexes.getOrDefault(category, 0) > 0;
    }

    public void nextPage(String category) {
        if (hasNextPage(category)) {
            categoryIndexes.put(category, categoryIndexes.get(category) + PAGE_SIZE);
        }
    }

    public void previousPage(String category) {
        if (hasPreviousPage(category)) {
            categoryIndexes.put(category, categoryIndexes.get(category) - PAGE_SIZE);
        }
    }

    ////-------------------
    /**
     * Gets the currently visible popular movie.
     *
     * @return the current popular Movie object
     */
    public Movie getCurrentPopularMovie() {
        if (!popularMovies.isEmpty()) {
            return popularMovies.get(popularIndex);
        }
        return null;
    }

    /**
     * Checks if a next popular movie exists.
     *
     * @return true if a next popular movie exists, false otherwise
     */
    public boolean hasNextPopular() {
        return popularIndex < popularMovies.size() - 1;
    }

    /**
     * Checks if a previous popular movie exists.
     *
     * @return true if a previous popular movie exists, false otherwise
     */
    public boolean hasPreviousPopular() {
        return popularIndex > 0;
    }

    /**
     * Advances to the next movie in the popular movies list.
     */
    public void nextPopular() {
        if (hasNextPopular()) {
            popularIndex++;
        }
    }

    /**
     * Moves back to the previous movie in the popular movies list.
     */
    public void previousPopular() {
        if (hasPreviousPopular()) {
            popularIndex--;
        }
    }

    /// ---- SEARCH -----
    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public List<Movie> getSearchResults() {
        return searchResults;
    }

    public void searchMovies() {
        searchResults.clear();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return;
        }

        String lowerQuery = searchQuery.toLowerCase();
        for (Movie m : allMovies) {
            if (m.getTitle().toLowerCase().contains(lowerQuery)) {
                searchResults.add(m);
            }
        }
    }

    public String getMovieTitleById(String movieId) {
        for (Movie movie : allMovies) {
            if (movie.getMovieId().equals(movieId)) {
                return movie.getTitle();
            }
        }
        return "Unknown Title";
    }

    public void recountInventory() {
        FacesContext context = FacesContext.getCurrentInstance();

        for (Movie movie : allMovies) {
            int activeBorrows = BorrowingDAO.countActiveBorrowingsByMovieId(movie.getMovieId());
            int total = movie.getCopiesAvailable() + activeBorrows;
            movie.setQuantity(total);
            MovieDAO.updateMovieInventory(movie);
        }

        loadMovies();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Inventory Recount", "Inventory recounted successfully."));
    }

    public void returnCopyToInventory(String movieId) {
        FacesContext context = FacesContext.getCurrentInstance();

        Movie movie = allMovies.stream()
                .filter(m -> m.getMovieId().equals(movieId))
                .findFirst()
                .orElse(null);

        if (movie == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Movie not found."));
            return;
        }

        int available = movie.getCopiesAvailable();
        int quantity = movie.getQuantity();

        if (available < quantity) {
            movie.setCopiesAvailable(available + 1);
            boolean success = MovieDAO.updateMovieInventory(movie);

            if (success) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Movie returned to inventory."));
                loadMovies();
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Failed to update inventory."));
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Notice", "All copies already returned."));
        }
    }

    public Movie getMovieByIdFromList(String movieId) {
        return allMovies.stream()
                .filter(m -> m.getMovieId().equals(movieId))
                .findFirst()
                .orElse(null);
    }

}
