package DAO;

import Modules.Borrowing;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import config.AppConfig;

/**
 * Data Access Object (DAO) for managing Borrowing records in the database.
 * Handles creation, update, retrieval, and status transitions for borrowings,
 * including loss reporting and confirmation workflows.
 */
public class BorrowingDAO {
    /**
     * Retrieves the borrowing history for a specific user by their ID.
     *
     * @param userId the ID of the user whose borrowing history should be
     * retrieved
     * @return a list of Borrowing objects representing the user's borrowing
     * history
     */
    public static List<Borrowing> getUserBorrowingHistory(String userId) {
        List<Borrowing> history = new ArrayList<>();
        String sql = "SELECT ID_BORROWING, ID_USER, ID_MOVIE, DATE_BORROW, DATE_RETURN "
                + "FROM BORROWINGS WHERE ID_USER = ? ORDER BY DATE_BORROW DESC";

        try (Connection conn = AppConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(new Borrowing(
                        rs.getString("ID_BORROWING"),
                        rs.getString("ID_USER"),
                        rs.getString("ID_MOVIE"),
                        rs.getDate("DATE_BORROW"),
                        rs.getDate("DATE_RETURN")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve borrowing history: " + e.getMessage());
        }

        return history;
    }

    /**
     * Adds a new borrowing record to the database. The return date is initially
     * set to null to indicate an active borrowing.
     *
     * @param borrowing the Borrowing object to be inserted
     * @return true if the insertion was successful, false otherwise
     */
    public static boolean addBorrowing(Borrowing borrowing) {
        String sql = "INSERT INTO BORROWINGS (ID_BORROWING, ID_USER, ID_MOVIE, DATE_BORROW, DATE_RETURN) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, borrowing.getBorrowingId());
            pstmt.setString(2, borrowing.getUserId());
            pstmt.setString(3, borrowing.getMovieId());
            pstmt.setDate(4, new java.sql.Date(borrowing.getBorrowDate().getTime()));
            pstmt.setDate(5, null);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Failed to add borrowing: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the list of currently borrowed movies for a specific user. Only
     * borrowings with no return date and a status of either null or
     * "PENDING_LOSS" are included.
     *
     * @param userId the ID of the user
     * @return a list of currently active Borrowing records for the user
     */
    public static List<Borrowing> getCurrentBorrowedByUser(String userId) {
        List<Borrowing> borrowings = new ArrayList<>();
        String sql = "SELECT * FROM BORROWINGS WHERE ID_USER = ? AND DATE_RETURN IS NULL AND (STATUS IS NULL OR STATUS = 'PENDING_LOSS')";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                borrowings.add(mapRowToBorrowing(rs));
            }
        } catch (SQLException e) {
            System.out.println("❌ Failed to fetch current borrowings: " + e.getMessage());
        }
        return borrowings;
    }

    /**
     * Marks a borrowing as returned by setting its return date.
     *
     * @param borrowingId the ID of the borrowing to update
     * @param returnDate the date the movie was returned
     * @return true if the update was successful, false otherwise
     */
    public static boolean markAsReturned(String borrowingId, java.sql.Date returnDate) {
        String sql = "UPDATE BORROWINGS SET DATE_RETURN = ? WHERE ID_BORROWING = ?";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, returnDate);
            stmt.setString(2, borrowingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ SQL Error (markAsReturned): " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves the list of active borrowings for a specific user. Active
     * borrowings are defined as those with no return date and no status set.
     *
     * @param userId the ID of the user
     * @return a list of active Borrowing records
     */
    public static List<Borrowing> getActiveBorrowingsByUser(String userId) {
        List<Borrowing> activeList = new ArrayList<>();
        String sql = "SELECT ID_BORROWING, ID_USER, ID_MOVIE, DATE_BORROW "
                + "FROM BORROWINGS WHERE ID_USER = ? AND DATE_RETURN IS NULL AND STATUS IS NULL";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Borrowing b = new Borrowing();
                b.setBorrowingId(rs.getString("ID_BORROWING"));
                b.setUserId(rs.getString("ID_USER"));
                b.setMovieId(rs.getString("ID_MOVIE"));
                b.setBorrowDate(rs.getDate("DATE_BORROW"));
                b.setReturnDate(null); //  not yet returned
                activeList.add(b);
            }
        } catch (SQLException e) {
            System.out.println("❌ Failed to fetch active borrowings: " + e.getMessage());
        }
        return activeList;
    }

    /**
     * Retrieves a single borrowing record from the database by its ID.
     *
     * @param borrowingId the ID of the borrowing to retrieve
     * @return the Borrowing object if found, or null if not found or on error
     */
    public static Borrowing getBorrowingById(String borrowingId) {
        String sql = "SELECT ID_BORROWING, ID_USER, ID_MOVIE, DATE_BORROW, DATE_RETURN FROM BORROWINGS WHERE ID_BORROWING = ?";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, borrowingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Borrowing(
                        rs.getString("ID_BORROWING"),
                        rs.getString("ID_USER"),
                        rs.getString("ID_MOVIE"),
                        rs.getDate("DATE_BORROW"),
                        rs.getDate("DATE_RETURN")
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Failed to fetch borrowing by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Counts the number of active borrowings for a given movie. Active
     * borrowings are defined as those with no return date.
     *
     * @param movieId the ID of the movie
     * @return the number of active borrowings for the specified movie
     */
    public static int countActiveBorrowingsByMovieId(String movieId) {
        String sql = "SELECT COUNT(*) FROM BORROWINGS WHERE ID_MOVIE = ? AND DATE_RETURN IS NULL";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, movieId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error (countActiveBorrowingsByMovieId): " + e.getMessage());
        }
        return 0;
    }

    /**
     * Marks a borrowing record as pending loss for the specified user and
     * movie. Only applies to active borrowings (no return date and no status
     * set).
     *
     * @param userId the ID of the user
     * @param movieId the ID of the movie
     * @return true if the update was successful, false otherwise
     */
    public static boolean markLossPending(String userId, String movieId) {
        String sql = "UPDATE BORROWINGS SET STATUS = '" + AppConfig.getStatusPendingLoss()
                + "' WHERE ID_USER = ? AND ID_MOVIE = ? AND DATE_RETURN IS NULL AND STATUS IS NULL";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Failed to mark loss as pending: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves a list of all borrowings marked as pending loss.
     *
     * @return a list of Borrowing objects with status "PENDING_LOSS"
     */
    public static List<Borrowing> getPendingLosses() {
        List<Borrowing> list = new ArrayList<>();
        String sql = "SELECT * FROM BORROWINGS WHERE STATUS = '" + AppConfig.getStatusPendingLoss() + "'";

        try (Connection conn = AppConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToBorrowing(rs));
            }

        } catch (SQLException e) {
            System.out.println("❌ Failed to get pending losses: " + e.getMessage());
        }

        return list;
    }

    /**
     * Confirms a pending movie loss by updating the borrowing status to
     * "CONFIRMED_LOSS" and setting the return date to the current date.
     *
     * @param borrowingId the ID of the borrowing to update
     * @return true if the update was successful, false otherwise
     */
    public static boolean confirmLossAndUpdate(String borrowingId) {
        String sql = "UPDATE BORROWINGS SET STATUS = '" + AppConfig.getStatusConfirmedLoss() + "', DATE_RETURN = CURRENT_DATE WHERE ID_BORROWING = ?";
        try (Connection conn = AppConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, borrowingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Failed to confirm loss: " + e.getMessage());
            return false;
        }
    }

    /**
     * Maps a single row from the ResultSet to a Borrowing object.
     *
     * @param rs the ResultSet containing the row data
     * @return a Borrowing object populated with data from the current row
     * @throws SQLException if a database access error occurs
     */
    private static Borrowing mapRowToBorrowing(ResultSet rs) throws SQLException {
        Borrowing b = new Borrowing();
        b.setBorrowingId(rs.getString("ID_BORROWING"));
        b.setUserId(rs.getString("ID_USER"));
        b.setMovieId(rs.getString("ID_MOVIE"));
        b.setBorrowDate(rs.getDate("DATE_BORROW"));
        b.setReturnDate(rs.getDate("DATE_RETURN"));
        b.setStatus(rs.getString("STATUS"));
        return b;
    }

    /**
     * Retrieves all borrowings made by a specific user, ordered by borrow date
     * (newest first).
     *
     * @param userId the ID of the user
     * @return a list of all Borrowing records associated with the user
     */
    public static List<Borrowing> getAllBorrowingsByUser(String userId) {
        List<Borrowing> borrowings = new ArrayList<>();

        try (Connection conn = AppConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM BORROWINGS WHERE ID_USER = ? ORDER BY DATE_BORROW DESC")) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Borrowing b = mapRowToBorrowing(rs);
                borrowings.add(b);
            }
        } catch (SQLException e) {
            System.out.println("❌ Failed to fetch borrowing history: " + e.getMessage());
        }
        return borrowings;
    }

}
