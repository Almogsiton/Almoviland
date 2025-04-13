package DAO;

import Modules.Movie;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import config.AppConfig;

/**
 * Data Access Object (DAO) for managing movies in the database. Provides
 * methods to retrieve, add, update, and delete movies, including associated
 * categories and poster images.
 */
public class MovieDAO {

    /**
     * Retrieves all movies from the database along with their categories.
     *
     * @return List of all movies.
     */
  public static List<Movie> getAllMovies() {
    List<Movie> movies = new ArrayList<>();
    Map<String, String> movieCategoriesMap = new HashMap<>();

    String sqlMovies = "SELECT MOVIE_ID, TITLE, DESCRIPTION, COPIES_AVAILABLE, QUANTITY, DATE_RELEASE, POSTER_IMAGE FROM MOVIES";
    String sqlCategories = "SELECT mc.MOVIE_ID, c.NAME_CATEGORY FROM MOVIE_CATEGORIES mc "
            + "JOIN CATEGORIES c ON mc.CATEGORY_ID = c.CATEGORY_ID";

    try (Connection conn = DriverManager.getConnection(
            AppConfig.getDatabaseUrl(),
            AppConfig.getDatabaseUser(),
            AppConfig.getDatabasePassword());
         Statement stmtMovies = conn.createStatement();
         Statement stmtCategories = conn.createStatement();
         ResultSet rsMovies = stmtMovies.executeQuery(sqlMovies);
         ResultSet rsCategories = stmtCategories.executeQuery(sqlCategories)) {

        // Load category names
        while (rsCategories.next()) {
            String movieId = rsCategories.getString("MOVIE_ID");
            String categoryName = rsCategories.getString("NAME_CATEGORY");
            movieCategoriesMap.merge(movieId, categoryName, (oldValue, newValue) -> oldValue + ", " + newValue);
        }

        // Load movies along with poster images
        while (rsMovies.next()) {
            String movieId = rsMovies.getString("MOVIE_ID");
            String categoryNames = movieCategoriesMap.getOrDefault(movieId, "No Category");

            Movie movie = new Movie(
                    movieId,
                    rsMovies.getString("TITLE"),
                    rsMovies.getString("DESCRIPTION"),
                    rsMovies.getInt("COPIES_AVAILABLE"),
                    rsMovies.getInt("QUANTITY"),
                    rsMovies.getDate("DATE_RELEASE"),
                    categoryNames
            );

            // Set poster image
            movie.setPosterImage(rsMovies.getBytes("POSTER_IMAGE"));

            movies.add(movie);
        }

    } catch (SQLException e) {
        System.err.println("❌ SQL Error: " + e.getMessage());
    }

    return movies;
}


    /**
     * Adds a new movie and its categories to the database.
     *
     * @param movie The movie to add.
     * @param selectedCategories The associated category IDs.
     * @return true if added successfully, false otherwise.
     */
    public static boolean addMovie(Movie movie, List<String> selectedCategories) {
        String sqlMovie = "INSERT INTO MOVIES (MOVIE_ID, TITLE, DESCRIPTION, COPIES_AVAILABLE, QUANTITY, DATE_RELEASE, POSTER_IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlCategory = "INSERT INTO MOVIE_CATEGORIES (MOVIE_ID, CATEGORY_ID) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), AppConfig.getDatabaseUser(), AppConfig.getDatabasePassword())) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtMovie = conn.prepareStatement(sqlMovie); PreparedStatement pstmtCategory = conn.prepareStatement(sqlCategory)) {

                pstmtMovie.setString(1, movie.getMovieId());
                pstmtMovie.setString(2, movie.getTitle());
                pstmtMovie.setString(3, movie.getDescription());
                pstmtMovie.setInt(4, movie.getCopiesAvailable());
                pstmtMovie.setInt(5, movie.getQuantity());
                pstmtMovie.setDate(6, new java.sql.Date(movie.getDateRelease().getTime()));
                pstmtMovie.setBytes(7, movie.getPosterImage() != null ? movie.getPosterImage() : new byte[0]);

                if (pstmtMovie.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                for (String categoryId : selectedCategories) {
                    pstmtCategory.setString(1, movie.getMovieId());
                    pstmtCategory.setString(2, categoryId);
                    pstmtCategory.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ SQL Error: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing movie and its categories.
     *
     * @param movie The movie to update.
     * @param selectedCategories New list of associated category IDs.
     * @return true if updated successfully, false otherwise.
     */
    public static boolean updateMovie(Movie movie, List<String> selectedCategories) {
        String sqlUpdateMovie = "UPDATE MOVIES SET TITLE=?, DESCRIPTION=?, COPIES_AVAILABLE=?, QUANTITY=?, DATE_RELEASE=? WHERE MOVIE_ID=?";
        String sqlDeleteCategories = "DELETE FROM MOVIE_CATEGORIES WHERE MOVIE_ID=?";
        String sqlInsertCategory = "INSERT INTO MOVIE_CATEGORIES (MOVIE_ID, CATEGORY_ID) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), AppConfig.getDatabaseUser(), AppConfig.getDatabasePassword())) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtMovie = conn.prepareStatement(sqlUpdateMovie); PreparedStatement pstmtDeleteCategories = conn.prepareStatement(sqlDeleteCategories); PreparedStatement pstmtInsertCategory = conn.prepareStatement(sqlInsertCategory)) {

                pstmtMovie.setString(1, movie.getTitle());
                pstmtMovie.setString(2, movie.getDescription());
                pstmtMovie.setInt(3, movie.getCopiesAvailable());
                pstmtMovie.setInt(4, movie.getQuantity());
                pstmtMovie.setDate(5, new java.sql.Date(movie.getDateRelease().getTime()));
                pstmtMovie.setString(6, movie.getMovieId());

                if (pstmtMovie.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                pstmtDeleteCategories.setString(1, movie.getMovieId());
                pstmtDeleteCategories.executeUpdate();

                for (String categoryId : selectedCategories) {
                    pstmtInsertCategory.setString(1, movie.getMovieId());
                    pstmtInsertCategory.setString(2, categoryId);
                    pstmtInsertCategory.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ SQL Error: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a movie and its associated categories.
     *
     * @param movieId The ID of the movie to delete.
     * @return true if deleted successfully, false otherwise.
     */
    public static boolean deleteMovie(String movieId) {
        String sqlDeleteCategories = "DELETE FROM MOVIE_CATEGORIES WHERE MOVIE_ID=?";
        String sqlDeleteMovie = "DELETE FROM MOVIES WHERE MOVIE_ID=?";

        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), AppConfig.getDatabaseUser(), AppConfig.getDatabasePassword())) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtCategories = conn.prepareStatement(sqlDeleteCategories); PreparedStatement pstmtMovie = conn.prepareStatement(sqlDeleteMovie)) {

                pstmtCategories.setString(1, movieId);
                pstmtCategories.executeUpdate();

                pstmtMovie.setString(1, movieId);
                int rowsAffected = pstmtMovie.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ SQL Error: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a movie with the given title exists.
     *
     * @param title The title to check.
     * @return true if the movie exists, false otherwise.
     */
    public static boolean movieExists(String title) {
        String sql = "SELECT COUNT(*) FROM MOVIES WHERE TITLE = ?";

        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), AppConfig.getDatabaseUser(), AppConfig.getDatabasePassword()); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates inventory quantity and available copies of a movie.
     *
     * @param movie The movie object to update.
     * @return true if updated successfully, false otherwise.
     */
    public static boolean updateMovieInventory(Movie movie) {
        String sql = "UPDATE MOVIES SET QUANTITY = ?, COPIES_AVAILABLE = ? WHERE MOVIE_ID = ?";

        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), AppConfig.getDatabaseUser(), AppConfig.getDatabasePassword()); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Math.max(movie.getQuantity(), 0));
            pstmt.setInt(2, Math.max(movie.getCopiesAvailable(), 0));
            pstmt.setString(3, movie.getMovieId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the poster image of a specific movie.
     *
     * @param movieId The ID of the movie.
     * @return Byte array of the poster image, or null if not found.
     */
//     public static byte[] getMovieImage(String movieId) {
//        String sql = "SELECT POSTER_IMAGE FROM MOVIES WHERE MOVIE_ID = ?";
//
//        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), AppConfig.getDatabaseUser(), AppConfig.getDatabasePassword()); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, movieId);
//            ResultSet rs = pstmt.executeQuery();
//
//            if (rs.next()) {
//                return rs.getBytes("POSTER_IMAGE");
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ SQL Error: " + e.getMessage());
//        }
//        return null;
//    }
// 
    public static Movie getMovieById(String movieId) {
        String sqlMovie = "SELECT MOVIE_ID, TITLE, DESCRIPTION, COPIES_AVAILABLE, QUANTITY, DATE_RELEASE "
                + "FROM MOVIES WHERE MOVIE_ID = ?";

        String sqlCategories = "SELECT C.NAME_CATEGORY FROM MOVIE_CATEGORIES MC "
                + "JOIN CATEGORIES C ON MC.CATEGORY_ID = C.CATEGORY_ID "
                + "WHERE MC.MOVIE_ID = ?";

        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); PreparedStatement pstmtMovie = conn.prepareStatement(sqlMovie); PreparedStatement pstmtCategories = conn.prepareStatement(sqlCategories)) {

            pstmtMovie.setString(1, movieId);
            ResultSet rsMovie = pstmtMovie.executeQuery();

            if (rsMovie.next()) {
                // שליפת שמות הקטגוריות
                StringBuilder categoryNames = new StringBuilder();
                pstmtCategories.setString(1, movieId);
                ResultSet rsCategories = pstmtCategories.executeQuery();
                while (rsCategories.next()) {
                    if (categoryNames.length() > 0) {
                        categoryNames.append(", ");
                    }
                    categoryNames.append(rsCategories.getString("NAME_CATEGORY"));
                }

                return new Movie(
                        rsMovie.getString("MOVIE_ID"),
                        rsMovie.getString("TITLE"),
                        rsMovie.getString("DESCRIPTION"),
                        rsMovie.getInt("COPIES_AVAILABLE"),
                        rsMovie.getInt("QUANTITY"),
                        rsMovie.getDate("DATE_RELEASE"),
                        categoryNames.toString()
                );
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error (getMovieById): " + e.getMessage());
        }

        return null;
    }

    public static String getMovieTitleById(String movieId) {
        String title = "Unknown Title";
        String sql = "SELECT TITLE FROM MOVIES WHERE ID_MOVIE = ?";

        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                title = rs.getString("TITLE");
            }

        } catch (SQLException e) {
            System.out.println("❌ Failed to fetch movie title: " + e.getMessage());
        }

        return title;
    }

}
