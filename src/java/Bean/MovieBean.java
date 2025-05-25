package Bean;

import Utils.PageController;
import Modules.Movie;
import Modules.Category;
import DAO.ReviewDAO;
import DAO.BorrowingDAO;
import DAO.CategoryDAO;
import DAO.MovieDAO;
import Utils.DateUtils;
import Utils.MathUtils;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Part;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import config.AppConfig;
import jakarta.inject.Inject;
import java.util.HashMap;
import jakarta.enterprise.context.SessionScoped;
import jakarta.annotation.PostConstruct;

/**
 * Managed Bean responsible for managing movies in the system. Handles movie
 * creation, deletion, inventory updates, image uploads, category grouping,
 * search functionality, and popular movie pagination.
 *
 * Session-scoped and tightly integrated with JSF views and DAO layer.
 */
@Named
@SessionScoped
public class MovieBean implements Serializable {

    private List<Movie> allMovies; // List of all movies loaded from the database
    private List<Category> allCategories; // List of all available categories
    private Movie newMovie; // New movie instance used in the add movie form
    private List<String> selectedCategories; // IDs of categories selected for the new or edited movie
    private Part uploadedFile; // Uploaded image file (movie poster)
    private String minDate; // Minimum release date for search filters
    private String maxDate; // Maximum release date for search filters
    private Movie selectedMovie; // Currently selected movie (for editing, viewing, etc.)
    private int addCopiesCount; // Number of copies to add to a movie’s inventory
    @Inject
    private PageController pageController; // Controller for JSF page navigation
    private Map<String, List<Movie>> moviesByCategory; // Grouped movies by category for homepage display
    private Map<String, Integer> categoryIndexes; // Current page index per category for carousel navigation
    private List<Movie> popularMovies; // List of most popular movies (e.g., by rating or views)
    private int popularIndex = 0; // Current page index in the popular movies carousel
    private int searchPageIndex = 0; // Current page index in the search results
    private final int moviesPerPage = AppConfig.getMoviesPerPage(); // Number of movies displayed per page
    private String searchQuery = ""; // Current search query string
    private final List<Movie> searchResults = new ArrayList<>(); // List of search results matching the query
    private int lostCopiesCount; // Number of lost copies to be removed from inventory
    private final Map<String, String> imageCache = new HashMap<>(); // Cache of base64-encoded poster images by movieId

    /**
     * Constructs a new MovieBean instance and initializes the movie list,
     * category list, and release date limits.
     */
    public MovieBean() {
    }

    /**
     * Initializes the MovieBean after construction. * This method is
     * automatically called by the container after dependency injection. It
     * prepares the bean state by: - Creating a new movie object and empty
     * lists/maps - Loading all movies and categories from the database -
     * Setting search date limits - Preloading base64-encoded images for
     * performance
     */
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

    // === Getters and Setters ===
    public List<Movie> getMovies() {
        return allMovies;
    }

    public List<Category> getCategories() {
        return allCategories;
    }

    public List<String> getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(List<String> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    public String getMinDate() {
        return minDate;
    }

    /**
     * @return the maximum allowed date for selecting a movie release date.
     */
    public String getMaxDate() {
        return maxDate;
    }

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public Movie getSelectedMovie() {
        return selectedMovie;
    }

    public void setSelectedMovie(Movie selectedMovie) {
        this.selectedMovie = selectedMovie;
    }

    public int getAddCopiesCount() {
        return addCopiesCount;
    }

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
     * Loads and caches poster images for all movies. For each movie: - If a
     * poster image exists, it is encoded as a Base64 string and stored in the
     * image cache. - If no image exists, a default image path is stored
     * instead. This improves performance by avoiding repeated encoding and
     * enables quick access to images in the UI.
     */
    private void loadImages() {
        for (Movie movie : allMovies) {
            byte[] imageBytes = movie.getPosterImage();
            String movieId = movie.getMovieId();

            if (imageBytes != null && imageBytes.length > 0) {

                String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
                imageCache.put(movieId, base64Image);
            } else {
                System.out.println("No image found for movie: " + movieId + ", using default.");
                imageCache.put(movieId, "/resources/images/default.PNG");
            }
        }
    }

    /**
     * Loads the list of all categories from the database and stores it in the
     * local list. Used during bean initialization to support movie-category
     * associations in the UI.
     */
    private void loadCategories() {
        allCategories = CategoryDAO.getAllCategories();
    }

    /**
     * Loads all movies from the database and organizes them for display. This
     * method: - Retrieves all movies and stores them in the local list. - Sorts
     * movies by average rating in descending order. - Identifies popular movies
     * (rating ≥ 4.0). - Groups movies by category for home page presentation. -
     * Initializes pagination indexes per category. - Resets the popular movie
     * index. - Loads and caches Base64-encoded poster images.
     */
    public void loadMovies() {
        List<Movie> movies = MovieDAO.getAllMovies();
        this.allMovies = movies;
        movies.sort((m1, m2) -> {
            double r1 = ReviewDAO.getAverageRating(m1.getMovieId());
            double r2 = ReviewDAO.getAverageRating(m2.getMovieId());
            return Double.compare(r2, r1);
        });
        popularMovies.clear();
        moviesByCategory.clear();
        categoryIndexes.clear();
        for (Movie movie : movies) {
            if (ReviewDAO.getAverageRating(movie.getMovieId()) >= AppConfig.getPopularRatingThreshold()) {
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
        loadImages();
    }

    /**
     * Adds a new movie to the system after validating input fields and handling
     * poster image upload.
     *
     * This method: - Validates required fields: title, categories, and release
     * date. - Checks for duplicate movie titles in the database. - Sets initial
     * properties including a generated movie ID, category names, and copy
     * count. - Validates and reads the uploaded poster image if provided. -
     * Attempts to insert the new movie into the database. - On success, reloads
     * the movie list and resets form fields. - Displays appropriate success or
     * error messages in the UI.
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
        newMovie.setCopiesAvailable(newMovie.getQuantity());
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
     * Deletes a movie and its associated category links from the database. This
     * method: - Calls the DAO to remove the movie record by ID. - On success,
     * reloads the movie list and shows a success message. - On failure, shows
     * an error message to the user.
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
     * loss. If the resulting quantity is negative, an error message is
     * displayed and the operation is aborted. On success, the movie inventory
     * is updated and the movie list is reloaded.
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
     * Decreases the total and available copies of the selected movie using the
     * value from lostCopiesCount. Intended for use in JSF action binding
     * without passing parameters.
     */
    public void movieLostByCompany() {
        movieLostByCompany(1);
    }

    /**
     * Loads a selected movie from request parameters and sets it as the current
     * editable movie. If the movie is found in the local list, it is set as
     * selected and navigation is triggered to the editing page. If not found, a
     * message is logged to the console.
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
     * The total quantity and available copies are updated accordingly.
     * Available copies will not exceed the total quantity. Displays a success
     * or error message based on the outcome.
     */
    public void addCopiesToInventory() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (selectedMovie != null && addCopiesCount > 0) {
            selectedMovie.setQuantity(selectedMovie.getQuantity() + addCopiesCount);
            int updatedAvailable = MathUtils.minSum(
                    selectedMovie.getCopiesAvailable(), addCopiesCount, selectedMovie.getQuantity());
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
     * Retrieves and returns the base64-encoded poster image of a movie. If the
     * image is not found in the cache, a default placeholder image path is
     * returned.
     *
     * @param movieId the ID of the movie
     * @return base64 image string if available, otherwise a default image path
     */
    public String getMovieImageBase64(String movieId) {
        return imageCache.getOrDefault(movieId, AppConfig.getDefaultImagePath());
    }

    /**
     * Returns a list of all category names that contain at least one movie.
     * Extracts the keys from the moviesByCategory map.
     *
     * @return list of category names
     */
    public List<String> getCategoryNames() {
        return new ArrayList<>(moviesByCategory.keySet());
    }

    /**
     * Returns a sublist of movies for the specified category based on the
     * current page index. Used to implement pagination in the home page
     * category sections.
     *
     * @param category the name of the category
     * @return list of movies visible on the current page for the given category
     */
    public List<Movie> getVisibleMoviesForCategory(String category) {
        List<Movie> all = moviesByCategory.getOrDefault(category, new ArrayList<>());
        int index = categoryIndexes.getOrDefault(category, 0);
        int toIndex = Math.min(index + moviesPerPage, all.size());
        return all.subList(index, toIndex);
    }

    /**
     * Checks if there is a next page of movies available for the given
     * category.
     *
     * @param category the name of the category
     * @return true if more movies exist beyond the current page
     */
    public boolean hasNextPage(String category) {
        List<Movie> all = moviesByCategory.getOrDefault(category, new ArrayList<>());
        int index = categoryIndexes.getOrDefault(category, 0);
        return index + moviesPerPage < all.size();
    }

    /**
     * Checks if there is a previous page of movies available for the given
     * category.
     *
     * @param category the name of the category
     * @return true if the current page is not the first
     */
    public boolean hasPreviousPage(String category) {
        return categoryIndexes.getOrDefault(category, 0) > 0;
    }

    /**
     * Advances the movie list view to the next page for the specified category.
     * Only proceeds if a next page exists.
     *
     * @param category the name of the category
     */
    public void nextPage(String category) {
        if (hasNextPage(category)) {
            categoryIndexes.put(category, categoryIndexes.get(category) + moviesPerPage);
        }
    }

    /**
     * Moves the movie list view to the previous page for the specified
     * category. Only proceeds if a previous page exists.
     *
     * @param category the name of the category
     */
    public void previousPage(String category) {
        if (hasPreviousPage(category)) {
            categoryIndexes.put(category, categoryIndexes.get(category) - moviesPerPage);
        }
    }

    /**
     * Returns the currently visible popular movie based on the popular index.
     * If no popular movies exist, returns null.
     *
     * @return the current popular Movie object or null if none
     */
    public Movie getCurrentPopularMovie() {
        if (!popularMovies.isEmpty()) {
            return popularMovies.get(popularIndex);
        }
        return null;
    }

    /**
     * Checks if there is a next popular movie available after the current
     * index.
     *
     * @return true if a next popular movie exists, false otherwise
     */
    public boolean hasNextPopular() {
        return popularIndex < popularMovies.size() - 1;
    }

    /**
     * Checks if there is a previous popular movie available before the current
     * index.
     *
     * @return true if a previous popular movie exists, false otherwise
     */
    public boolean hasPreviousPopular() {
        return popularIndex > 0;
    }

    /**
     * Advances to the next movie in the popular movies list. Increments the
     * index only if a next popular movie exists.
     */
    public void nextPopular() {
        if (hasNextPopular()) {
            popularIndex++;
        }
    }

    /**
     * Moves back to the previous movie in the popular movies list. Decrements
     * the index only if a previous popular movie exists.
     */
    public void previousPopular() {
        if (hasPreviousPopular()) {
            popularIndex--;
        }
    }

    /// ---- SEARCH -----
    /**
     * Returns the current search query entered by the user.
     *
     * @return the search query string
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * Sets the search query entered by the user.
     *
     * @param searchQuery the search keyword to filter movies
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * Returns the list of movies that match the current search query.
     *
     * @return list of matching search results
     */
    public List<Movie> getSearchResults() {
        return searchResults;
    }

    /**
     * Filters the list of all movies based on the current search query.
     * Matching is performed by checking if the movie title contains the query
     * (case-insensitive). The search results list is cleared and page index is
     * reset to 0 before filtering.
     */
    public void searchMovies() {
        searchResults.clear();
        searchPageIndex = 0; // resets the search result page index
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

    /**
     * Returns a sublist of search results for the current search page. Used for
     * paginating search results in the UI.
     *
     * @return list of movies visible on the current search page
     */
    public List<Movie> getVisibleSearchResults() {
        int toIndex = Math.min(searchPageIndex + moviesPerPage, searchResults.size());
        return searchResults.subList(searchPageIndex, toIndex);
    }

    /**
     * Checks if there is a next page of search results available.
     *
     * @return true if more search results exist beyond the current page
     */
    public boolean hasNextSearchPage() {
        return searchPageIndex + moviesPerPage < searchResults.size();
    }

    /**
     * Checks if there is a previous page of search results available.
     *
     * @return true if the current page is not the first
     */
    public boolean hasPreviousSearchPage() {
        return searchPageIndex > 0;
    }

    /**
     * Advances to the next page of search results. Only moves forward if a next
     * page exists.
     */
    public void nextSearchPage() {
        if (hasNextSearchPage()) {
            searchPageIndex += moviesPerPage;
        }
    }

    /**
     * Moves back to the previous page of search results. Only moves backward if
     * a previous page exists.
     */
    public void previousSearchPage() {
        if (hasPreviousSearchPage()) {
            searchPageIndex -= moviesPerPage;
        }
    }

    /**
     * Retrieves the title of a movie given its ID.
     *
     * @param movieId the ID of the movie
     * @return the movie title, or "Unknown Title" if not found
     */
    public String getMovieTitleById(String movieId) {
        for (Movie movie : allMovies) {
            if (movie.getMovieId().equals(movieId)) {
                return movie.getTitle();
            }
        }
        return AppConfig.getDefaultMovieTitle();

    }

    /**
     * Recalculates the total quantity of each movie based on its available and
     * borrowed copies. Updates the inventory in the database for all movies.
     * After updating, reloads the movie list and shows a success message in the
     * UI.
     */
    public void recountInventory() {
        FacesContext context = FacesContext.getCurrentInstance();
        for (Movie movie : allMovies) {
            int activeBorrows = BorrowingDAO.countActiveBorrowingsByMovieId(movie.getMovieId());
            int total = MathUtils.sum(movie.getCopiesAvailable(), activeBorrows);
            movie.setQuantity(total);
            MovieDAO.updateMovieInventory(movie);
        }
        loadMovies();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Inventory Recount", "Inventory recounted successfully."));
    }

    /**
     * Returns a single copy of the specified movie to the inventory. Increases
     * the available copies count if it is less than the total quantity. Updates
     * the database and reloads the movie list if successful. Displays
     * appropriate messages for each scenario.
     *
     *
     * @param movieId the ID of the movie to return to inventory
     */
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

    /**
     * Searches the local movie list for a movie with the given ID.
     *
     * @param movieId the ID of the movie to find
     * @return the matching Movie object, or null if not found
     */
    public Movie getMovieByIdFromList(String movieId) {
        return allMovies.stream()
                .filter(m -> m.getMovieId().equals(movieId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a sublist of popular movies based on the current page index. Used
     * for paginating popular movies on the home page.
     *
     * @return list of popular movies for the current page
     */
    public List<Movie> getVisiblePopularMovies() {
        int toIndex = Math.min(popularIndex + moviesPerPage, popularMovies.size());
        return popularMovies.subList(popularIndex, toIndex);
    }

    /**
     * Checks if there is a next page of popular movies.
     *
     * @return true if more popular movies exist beyond the current page
     */
    public boolean hasNextPopularPage() {
        return popularIndex + moviesPerPage < popularMovies.size();
    }

    /**
     * Checks if there is a previous page of popular movies.
     *
     * @return true if the current page is not the first
     */
    public boolean hasPreviousPopularPage() {
        return popularIndex > 0;
    }

    /**
     * Advances to the next page of popular movies, if available.
     */
    public void nextPopularPage() {
        if (hasNextPopularPage()) {
            popularIndex += moviesPerPage;
        }
    }

    /**
     * Moves to the previous page of popular movies, if available.
     */
    public void previousPopularPage() {
        if (hasPreviousPopularPage()) {
            popularIndex -= moviesPerPage;
        }
    }

    /**
     * Returns a copy of the currently visible popular movies list. Useful for
     * safe iteration or rendering without modifying the original list.
     *
     * @return a new list containing the current visible popular movies
     */
    public List<Movie> getVisiblePopularMoviesCopy() {
        List<Movie> visiblePopular = getVisiblePopularMovies();
        return new ArrayList<>(visiblePopular);
    }
}
