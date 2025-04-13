package config;

/**
 * Application Configuration Class.
 * 
 * This class provides centralized access to constant configuration values
 * used throughout the application, such as admin credentials, default roles,
 * page navigation paths, database connection settings, and file upload limits.
 */
public class AppConfig {

    // Admin default credentials and settings
    private static final String ADMIN_ID = "1";
    private static final String ADMIN_NAME = "almoviland";
    private static final String ADMIN_EMAIL = "almoviland@gmail.com";
    private static final String ADMIN_PASSWORD = "almoviland"; // will be encrypted before use
    private static final String ADMIN_ROLE = "ADMIN";
    private static final int ADMIN_LIMIT_BORROW_MAX = 3;

    // Database connection settings
    private static final String DATABASE_URL = "jdbc:derby://localhost:1527/almoviland";
    private static final String DATABASE_USER = "almoviland";
    private static final String DATABASE_PASSWORD = "almoviland";

    // File upload settings
    private static final long MAX_IMAGE_UPLOAD_SIZE = 2 * 1024 * 1024;

    // Page navigation settings
    private static final String DEFAULT_PAGE = "home";
    private static final String REGISTER_PAGE = "register";
    private static final String LOGIN_PAGE = "login";

    // Default user role and borrowing settings
    private static final String DEFAULT_USER_ROLE = "USER";
    private static final int DEFAULT_BORROW_LIMIT = 5;

    /**
     * @return the default admin ID
     */
    public static String getAdminId() {
        return ADMIN_ID;
    }

    /**
     * @return the default admin name
     */
    public static String getAdminName() {
        return ADMIN_NAME;
    }

    /**
     * @return the default admin email
     */
    public static String getAdminEmail() {
        return ADMIN_EMAIL;
    }

    /**
     * @return the default admin password (should be encrypted before use)
     */
    public static String getAdminPassword() {
        return ADMIN_PASSWORD;
    }

    /**
     * @return the admin role string
     */
    public static String getAdminRole() {
        return ADMIN_ROLE;
    }

    /**
     * @return the maximum number of items an admin can borrow
     */
    public static int getAdminLimitBorrowMax() {
        return ADMIN_LIMIT_BORROW_MAX;
    }

    /**
     * @return the database connection URL
     */
    public static String getDatabaseUrl() {
        return DATABASE_URL;
    }

    /**
     * @return the database username
     */
    public static String getDatabaseUser() {
        return DATABASE_USER;
    }

    /**
     * @return the database password
     */
    public static String getDatabasePassword() {
        return DATABASE_PASSWORD;
    }

    /**
     * @return the maximum image upload size in bytes
     */
    public static long getMaxImageUploadSize() {
        return MAX_IMAGE_UPLOAD_SIZE;
    }

    /**
     * @return the name of the default landing page
     */
    public static String getDefaultPage() {
        return DEFAULT_PAGE;
    }

    /**
     * @return the default role assigned to new users
     */
    public static String getDefaultUserRole() {
        return DEFAULT_USER_ROLE;
    }

    /**
     * @return the default number of movies a new user can borrow
     */
    public static int getDefaultBorrowLimit() {
        return DEFAULT_BORROW_LIMIT;
    }

    /**
     * @return the navigation path to the register page
     */
    public static String getRegisterPage() {
        return REGISTER_PAGE;
    }

    /**
     * @return the navigation path to the login page
     */
    public static String getLoginPage() {
        return LOGIN_PAGE;
    }
}
