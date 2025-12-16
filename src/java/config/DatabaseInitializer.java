package config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import DAO.UserDAO;

/**
 * WebListener that initializes the database schema when the application starts.
 * This ensures tables exist before the application tries to use them.
 */
@WebListener
public class DatabaseInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("🚀 Initializing Database Schema...");
        try (Connection conn = AppConfig.getConnection(); Statement stmt = conn.createStatement()) {

            // 1. USERS Table
            createTable(stmt, "USERS", "CREATE TABLE USERS ("
                    + "USER_ID VARCHAR(50) NOT NULL PRIMARY KEY, "
                    + "NAME VARCHAR(100), "
                    + "EMAIL VARCHAR(100) UNIQUE, " // Added UNIQUE constraint for email
                    + "PASSWORD VARCHAR(255), "
                    + "ROLE VARCHAR(20), "
                    + "LIMIT_BORROW_MAX INT, "
                    + "DATE_REGISTRATION DATE"
                    + ")");

            // 2. CATEGORIES Table
            createTable(stmt, "CATEGORIES", "CREATE TABLE CATEGORIES ("
                    + "CATEGORY_ID VARCHAR(50) NOT NULL PRIMARY KEY, "
                    + "NAME_CATEGORY VARCHAR(100), "
                    + "DESCRIPTION VARCHAR(500), "
                    + "POSTER_IMAGE BLOB"
                    + ")");

            // 3. MOVIES Table
            createTable(stmt, "MOVIES", "CREATE TABLE MOVIES ("
                    + "MOVIE_ID VARCHAR(50) NOT NULL PRIMARY KEY, "
                    + "TITLE VARCHAR(100), "
                    + "DESCRIPTION VARCHAR(1000), "
                    + "COPIES_AVAILABLE INT, "
                    + "QUANTITY INT, "
                    + "DATE_RELEASE DATE, "
                    + "POSTER_IMAGE BLOB"
                    + ")");

            // 4. MOVIE_CATEGORIES Table (Join Table)
            createTable(stmt, "MOVIE_CATEGORIES", "CREATE TABLE MOVIE_CATEGORIES ("
                    + "MOVIE_ID VARCHAR(50) NOT NULL, "
                    + "CATEGORY_ID VARCHAR(50) NOT NULL, "
                    + "PRIMARY KEY (MOVIE_ID, CATEGORY_ID), "
                    + "FOREIGN KEY (MOVIE_ID) REFERENCES MOVIES(MOVIE_ID), "
                    + "FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES(CATEGORY_ID)"
                    + ")");

            // 5. REVIEWS Table
            createTable(stmt, "REVIEWS", "CREATE TABLE REVIEWS ("
                    + "REVIEW_ID VARCHAR(50) NOT NULL PRIMARY KEY, "
                    + "ID_USER VARCHAR(50), "
                    + "ID_MOVIE VARCHAR(50), "
                    + "COMMENT VARCHAR(1000), "
                    + "RATING INT, "
                    + "DATE_REVIEW TIMESTAMP, "
                    + "FOREIGN KEY (ID_USER) REFERENCES USERS(USER_ID), "
                    + "FOREIGN KEY (ID_MOVIE) REFERENCES MOVIES(MOVIE_ID)"
                    + ")");

            // 6. BORROWINGS Table
            createTable(stmt, "BORROWINGS", "CREATE TABLE BORROWINGS ("
                    + "ID_BORROWING VARCHAR(50) NOT NULL PRIMARY KEY, "
                    + "ID_USER VARCHAR(50), "
                    + "ID_MOVIE VARCHAR(50), "
                    + "DATE_BORROW DATE, "
                    + "DATE_RETURN DATE, "
                    + "STATUS VARCHAR(50), "
                    + "FOREIGN KEY (ID_USER) REFERENCES USERS(USER_ID), "
                    + "FOREIGN KEY (ID_MOVIE) REFERENCES MOVIES(MOVIE_ID)"
                    + ")");

            System.out.println("✅ Database Schema Initialized.");

            // Initialize Admin User
            UserDAO.createAdminIfNotExists();

        } catch (SQLException e) {
            System.err.println("❌ Database Initialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTable(Statement stmt, String tableName, String ddl) {
        try {
            stmt.executeUpdate(ddl);
            System.out.println("✅ Table " + tableName + " created.");
        } catch (SQLException e) {
            if (e.getSQLState().equals("X0Y32")) { // Derby code for "Table/View already exists"
                System.out.println("ℹ️ Table " + tableName + " already exists.");
            } else {
                System.err.println("❌ Error creating table " + tableName + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }
}
