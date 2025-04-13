package Bean;

import Utils.PageController;
import Modules.Borrowing;
import Modules.User;
import DAO.UserDAO;
import DAO.BorrowingDAO;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.List;
import jakarta.faces.context.ExternalContext;
import jakarta.inject.Named;
import config.AppConfig;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Managed Bean for handling user-related operations such as registration,
 * login, logout, user role management, and viewing borrowing history. This bean
 * is session scoped, meaning it is active for the duration of a user's session.
 */
@Named
@SessionScoped
public class UserBean implements Serializable {

    @Inject
    private PageController pageController;

    private List<User> users;
    private User newUser = new User();
    private User selectedUser;
    private String loginEmail;
    private String loginPassword;
    private User loggedInUser;
    private List<Borrowing> borrowingHistory;
    private String confirmPassword;
    private String creditCardNumber;
    private String expiryDate;
    private String cvc;
    private String expiryMonth;
    private String expiryYear;
    private int limitBorrowMax;

    /**
     * Ensures the user list is loaded from the database.
     */
    public void ensureUsersLoaded() {
        if (users == null || users.isEmpty()) {
            loadUsers();
        }
    }

    /**
     * Constructor - creates the first admin if no users exist.
     */
    public UserBean() {
        UserDAO.createAdminIfNotExists();
    }

    /**
     * Loads all users from the database.
     */
    public void loadUsers() {
        users = UserDAO.getAllUsers();
        System.out.println("🔄 Users Loaded: " + (users != null ? users.size() : 0));
    }

    /**
     * @return The list of all users.
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
            System.out.println("🔍 Entered Hash: " + enteredPasswordHash);
            System.out.println("🔍 Stored Hash: " + user.getPassword());

            if (user.getPassword().equals(enteredPasswordHash)) {
                externalContext.getSessionMap().put("loggedUser", user);
                loggedInUser = user;
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome!", "Login successful."));
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
     * Registers a new user. Validates password confirmation and checks for
     * email uniqueness.
     */
    public void registerUser() {
        FacesContext context = FacesContext.getCurrentInstance();

        // 🔐 אימות סיסמה חוזרת
        if (!newUser.getPassword().equals(confirmPassword)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Passwords do not match!"));
            return;
        }

        // 📧 בדיקת כפילות אימייל
        if (UserDAO.getUserByEmail(newUser.getEmail()) != null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Email is already in use!"));
            return;
        }

        // 💳 בדיקת כרטיס אשראי: מספר, CVC, תוקף
        if (creditCardNumber == null || !creditCardNumber.matches("\\d{16}")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Credit card number must be 16 digits."));
            return;
        }

        if (cvc == null || !cvc.matches("\\d{3}")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "CVC must be 3 digits."));
            return;
        }

        if (expiryMonth == null || expiryYear == null
                || !expiryMonth.matches("\\d{1,2}") || !expiryYear.matches("\\d{4}")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid expiry date."));
            return;
        }

        try {
            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);

            if (month < 1 || month > 12) {
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

        // 🧑 יצירת המשתמש בבסיס הנתונים (ההצפנה מתבצעת ב-DAO)
        boolean success = UserDAO.addUser(newUser);

        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Registration successful!"));
            newUser = new User();  // אתחול חדש
            confirmPassword = "";
            creditCardNumber = "";
            expiryMonth = "";
            expiryYear = "";
            cvc = "";

            pageController.setPage(AppConfig.getDefaultPage()); // מעבר לעמוד הבית
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Registration failed."));
        }
    }

    public List<String> getMonthOptions() {
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.format("%02d", i));
        }
        return months;
    }

    public List<String> getYearOptions() {
        List<String> years = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i <= 10; i++) {
            years.add(String.valueOf(currentYear + i));
        }
        return years;
    }

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

}
