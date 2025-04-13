package DAO;

import Modules.Borrowing;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import config.AppConfig;

/**
 * Data Access Object (DAO) class for retrieving borrowing history from the
 * database.
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

        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    public static boolean addBorrowing(Borrowing borrowing) {
        String sql = "INSERT INTO BORROWINGS (ID_BORROWING, ID_USER, ID_MOVIE, DATE_BORROW, DATE_RETURN) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, borrowing.getBorrowingId());
            pstmt.setString(2, borrowing.getUserId());
            pstmt.setString(3, borrowing.getMovieId());
            pstmt.setDate(4, new java.sql.Date(borrowing.getBorrowDate().getTime()));
            pstmt.setDate(5, null); // עדיין לא הוחזר

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("❌ Failed to add borrowing: " + e.getMessage());
            return false;
        }
    }

    public static List<Borrowing> getCurrentBorrowedByUser(String userId) {
        List<Borrowing> borrowings = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM BORROWINGS WHERE ID_USER = ? AND DATE_RETURN IS NULL")) {

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Borrowing b = new Borrowing(
                        rs.getString("ID_BORROWING"),
                        rs.getString("ID_USER"),
                        rs.getString("ID_MOVIE"),
                        rs.getDate("DATE_BORROW"),
                        rs.getDate("DATE_RETURN")
                );
                borrowings.add(b);
            }

        } catch (SQLException e) {
            System.out.println("❌ Failed to fetch current borrowings: " + e.getMessage());
        }

        return borrowings;
    }

    public static boolean markAsReturned(String borrowingId, java.sql.Date returnDate) {
    String sql = "UPDATE BORROWINGS SET DATE_RETURN = ? WHERE ID_BORROWING = ?";

    try (Connection conn = DriverManager.getConnection(
            AppConfig.getDatabaseUrl(),
            AppConfig.getDatabaseUser(),
            AppConfig.getDatabasePassword());
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setDate(1, returnDate);
        stmt.setString(2, borrowingId);

        return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
        System.err.println("❌ SQL Error (markAsReturned): " + e.getMessage());
    }

    return false;
}



    public static List<Borrowing> getActiveBorrowingsByUser(String userId) {
        List<Borrowing> activeList = new ArrayList<>();
        String sql = "SELECT ID_BORROWING, ID_USER, ID_MOVIE, DATE_BORROW "
                + "FROM BORROWINGS WHERE ID_USER = ? AND DATE_RETURN IS NULL";

        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Borrowing b = new Borrowing();
                b.setBorrowingId(rs.getString("ID_BORROWING"));
                b.setUserId(rs.getString("ID_USER"));
                b.setMovieId(rs.getString("ID_MOVIE"));
                b.setBorrowDate(rs.getDate("DATE_BORROW"));
                b.setReturnDate(null); // עדיין לא הוחזר
                activeList.add(b);
            }

        } catch (SQLException e) {
            System.out.println("❌ Failed to fetch active borrowings: " + e.getMessage());
        }

        return activeList;
    }

    public static Borrowing getBorrowingById(String borrowingId) {
        String sql = "SELECT ID_BORROWING, ID_USER, ID_MOVIE, DATE_BORROW, DATE_RETURN FROM BORROWINGS WHERE ID_BORROWING = ?";

        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDatabaseUrl(),
                AppConfig.getDatabaseUser(),
                AppConfig.getDatabasePassword()); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

    public static int countActiveBorrowingsByMovieId(String movieId) {
        String sql = "SELECT COUNT(*) FROM BORROWINGS WHERE ID_MOVIE = ? AND DATE_RETURN IS NULL";
        try (Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl(), AppConfig.getDatabaseUser(), AppConfig.getDatabasePassword()); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

}
