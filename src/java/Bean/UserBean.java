package Bean;

import Utils.PageController;
import Modules.Borrowing;
import Modules.User;
import DAO.UserDAO;
import DAO.BorrowingDAO;
import Utils.MathUtils;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.List;
import jakarta.faces.context.ExternalContext;
import jakarta.inject.Named;
import config.AppConfig;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Session-scoped managed bean that manages the state and actions of the
 * currently logged-in user in the system. Handles registration, login/logout,
 * role assignment, user management (for admins), and borrowing history.
 */
@Named
@SessionScoped
public class UserBean implements Serializable {

    @Inject
    private PageController pageController; // Controller for JSF page navigation
    private List<User> users; // List of all users in the system (admin view)
    private User newUser = new User(); // New user object used during registration
    private User selectedUser; // User selected by admin for management actions
    private String loginEmail; // Email entered during login
    private String loginPassword; // Password entered during login
    private User loggedInUser; // Currently authenticated user (stored in session)
    private List<Borrowing> borrowingHistory; // List of past borrowings for the logged-in user
    private String confirmPassword; // Password confirmation field for registration
    private String creditCardNumber; // Temporary credit card number entered at registration
    private String expiryDate; // Combined credit card expiry date string
    private String expiryMonth; // Credit card expiry month 
    private String expiryYear; // Credit card expiry year 
    private String cvc; // Credit card security code
    private int limitBorrowMax; // Maximum number of borrowings allowed for this user

    /**
     * Loads the user list from the database if it is not already loaded.
     */
    public void ensureUsersLoaded() {
        if (users == null || users.isEmpty()) {
            loadUsers();
        }
    }

    /**
     * Constructs the UserBean and ensures an admin user exists in the system.
     * Invokes the DAO to create the first admin if no users are found.
     */
    public UserBean() {
        UserDAO.createAdminIfNotExists();
    }

    /**
     * Loads all users from the database into the local list. Intended for admin
     * use to manage the list of registered users.
     */
    public void loadUsers() {
        users = UserDAO.getAllUsers();
        System.out.println("Users Loaded: " + (users != null ? users.size() : 0));
    }

    /**
     * @return the list of all users, loaded from the database if not already
     * available
     */
    public List<User> getUsers() {
        ensureUsersLoaded();
        return users;
    }

    /**
     * @return The new user being created during registration.
     */
    public User getNewUser() {
        return newUser;
    }

    /**
     * Sets the new user object for registration.
     *
     * @param newUser The user to register.
     */
    public void setNewUser(User newUser) {
        this.newUser = newUser;
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

    public int getBorrowLimit() {
        return AppConfig.getAdminLimitBorrowMax();
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

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * @return The user selected for editing or deletion.
     */
    public User getSelectedUser() {
        return selectedUser;
    }

    /**
     * Sets the selected user.
     *
     * @param selectedUser The user to mark as selected.
     */
    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    /**
     * @return The email entered in the login form.
     */
    public String getLoginEmail() {
        return loginEmail;
    }

    /**
     * Sets the login email.
     *
     * @param loginEmail The email address entered by the user.
     */
    public void setLoginEmail(String loginEmail) {
        this.loginEmail = loginEmail;
    }

    /**
     * @return The password entered in the login form.
     */
    public String getLoginPassword() {
        return loginPassword;
    }

    /**
     * Sets the login password.
     *
     * @param loginPassword The password entered by the user.
     */
    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    /**
     * @return The currently logged-in user, or null if no user is logged in.
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * @return The password confirmation entered during registration.
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    /**
     * Sets the password confirmation value.
     *
     * @param confirmPassword The confirmation password to validate against the
     * original.
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /**
     * @return The borrowing history of the selected user.
     */
    public List<Borrowing> getBorrowingHistory() {
        return borrowingHistory;
    }

    // Getter for limitBorrowMax
    public int getLimitBorrowMax() {
        return limitBorrowMax;
    }

    // Setter for limitBorrowMax
    public void setLimitBorrowMax(int limitBorrowMax) {
        this.limitBorrowMax = limitBorrowMax;
    }

    /**
     * Attempts to log in a user with the provided credentials. If successful,
     * stores the user in the session.
     */
    public void loginUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        User user = UserDAO.getUserByEmail(loginEmail);
        if (user != null) {
            String enteredPasswordHash = UserDAO.hashPassword(loginPassword);
            System.out.println("Entered Hash: " + enteredPasswordHash);
            System.out.println("Stored Hash: " + user.getPassword());
            if (user.getPassword().equals(enteredPasswordHash)) {
                externalContext.getSessionMap().put("loggedUser", user);
                loggedInUser = user;
                context.getExternalContext().getFlash().setKeepMessages(true);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome! Login successful.", ""));
                pageController.setPage(AppConfig.getDefaultPage());
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Invalid email or password."));
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Invalid email or password."));
        }
    }

    /**
     * Logs out the currently logged-in user and resets the login fields.
     */
    public void logoutUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        loggedInUser = null;
        loginEmail = null;
        loginPassword = null;
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "You have been logged out."));
        pageController.setPage(AppConfig.getDefaultPage());
    }

    /**
     * Toggles the user's role between USER and ADMIN.
     *
     * @param userId The user ID whose role will be toggled.
     */
    public void toggleUserRole(String userId) {
        FacesContext context = FacesContext.getCurrentInstance();
        boolean success = UserDAO.changeUserRole(userId);
        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "User role updated successfully!"));
            loadUsers();
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to update user role!"));
        }
    }

    /**
     * Deletes a user by ID.
     *
     * @param userId The user ID to delete.
     */
    public void deleteUser(String userId) {
        FacesContext context = FacesContext.getCurrentInstance();
        boolean success = UserDAO.deleteUser(userId);
        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "User deleted successfully!"));
            loadUsers();
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to delete user!"));
        }
    }

    /**
     * Loads borrowing history for the specified user.
     *
     * @param userId The user ID whose borrowing history to display.
     */
    public void viewUserBorrowingHistory(String userId) {
        borrowingHistory = BorrowingDAO.getUserBorrowingHistory(userId);
        pageController.setPage("userBorrowingHistory");
    }

    /**
     * Registers a new user. Validates password confirmation, ensures the email
     * is unique, and performs basic credit card validation (number, CVC,
     * expiry).
     */
    public void registerUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        // Validate password confirmation
        if (!newUser.getPassword().equals(confirmPassword)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Passwords do not match!"));
            return;
        }
        // ✅ Validate password strength
        if (!MathUtils.isStrongPassword(newUser.getPassword())) {
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "Password must contain at least one lowercase letter, one uppercase letter, and one number."
            ));
            return;
        }
        // Check if email already exists
        if (UserDAO.getUserByEmail(newUser.getEmail()) != null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Email is already in use!"));
            return;
        }
        // Validate credit card number
        if (creditCardNumber == null || !creditCardNumber.matches("\\d{" + AppConfig.getCreditCardLength() + "}")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Credit card number must be 16 digits."));
            return;
        }
        // Validate CVC
        if (cvc == null || !cvc.matches("\\d{" + AppConfig.getCvcLength() + "}")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "CVC must be 3 digits."));
            return;
        }
        // Validate expiry date format and range
        if (expiryMonth == null || expiryYear == null
                || !expiryMonth.matches("\\d{1,2}") || !expiryYear.matches("\\d{4}")) {// validate format: MM and YYYY
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid expiry date."));
            return;
        }
        try {
            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);
            if (month < AppConfig.getMinMonth() || month > AppConfig.getMaxMonth()) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid month (1-12)."));
                return;
            }
            java.time.YearMonth expiry = java.time.YearMonth.of(year, month);
            java.time.YearMonth now = java.time.YearMonth.now();
            if (expiry.isBefore(now)) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Card has expired."));
                return;
            }
        } catch (NumberFormatException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid expiry date format."));
            return;
        }
        // Attempt to add user to database
        boolean success = UserDAO.addUser(newUser);
        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Registration successful!"));
            resetRegistrationForm();
            pageController.setPage(AppConfig.getDefaultPage());
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Registration failed."));
        }
    }

    /**
     * Resets all registration-related input fields to their default values.
     */
    private void resetRegistrationForm() {
        newUser = new User();
        confirmPassword = "";
        creditCardNumber = "";
        expiryMonth = "";
        expiryYear = "";
        cvc = "";
    }

    /**
     * @return a list of month options (01–12) for credit card expiry dropdown
     */
    public List<String> getMonthOptions() {
        return MathUtils.generateMonthOptions(AppConfig.getMinMonth(), AppConfig.getMaxMonth());
    }

    /**
     * @return a list of credit card expiry year options, starting from the
     * current year and including the next configured number of years
     */
    public List<String> getYearOptions() {
        return MathUtils.generateYearOptions(AppConfig.getExpiryYearStartOffset(), AppConfig.getExpiryYearRange());
    }

    /**
     * Redirects to a given page if no user is currently logged in.
     *
     * @param pageIfNotLoggedIn the target page (without .xhtml extension) to
     * redirect to
     */
    public void redirectIfNotLoggedIn(String pageIfNotLoggedIn) {
        if (loggedInUser == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            try {
                context.getExternalContext().redirect(pageIfNotLoggedIn + ".xhtml");
            } catch (IOException e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error:", "Failed to redirect. Please try again."));
            }
        }
    }

    /**
     * @return a list of all users with the "ADMIN" role
     */
    public List<User> getAdmins() {
        loadUsers();
        List<User> admins = new ArrayList<>();
        for (User user : users) {
            if (AppConfig.getAdminRole().equalsIgnoreCase(user.getRole())) {
                admins.add(user);
            }
        }
        return admins;
    }

    /**
     * @return the monthly subscription price exposed to JSF
     */
    public double getMonthlySubscriptionPrice() {
        return AppConfig.getMonthlySubscriptionPrice();
    }

    /**
     * @return the penalty amount charged for reporting a lost movie
     */
    public double getLossChargeAmount() {
        return AppConfig.getLossChargeAmount();
    }
}
