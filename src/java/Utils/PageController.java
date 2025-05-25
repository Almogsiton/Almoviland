package Utils;

import Bean.UserBean;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import config.AppConfig;
import Bean.MovieBean;
import jakarta.inject.Inject;

/**
 * Managed Bean responsible for handling page navigation within the system.
 * Maintains and updates the current page the user is viewing.
 */
@Named("pageController")
@SessionScoped
public class PageController implements Serializable {

    @Inject
    private MovieBean movieBean;
    private String currentPage = AppConfig.getDefaultPage();// Holds the name of the current page the user is viewing

    /**
     * Returns the current page name.
     *
     * @return The name of the current page.
     */
    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * Sets the current page to the given page name. Also performs validation: -
     * Prevents admin page access if user is not an admin. - Logs an error if
     * the page name is null or empty.
     *
     * @param page The target page name to navigate to.
     */
    public void setPage(String page) {
        FacesContext context = FacesContext.getCurrentInstance();
        UserBean userBean = context.getApplication().evaluateExpressionGet(context, "#{userBean}", UserBean.class);

        if ("admin".equals(page) && (userBean.getLoggedInUser() == null || !"ADMIN".equals(userBean.getLoggedInUser().getRole()))) {
            System.out.println("⛔ Access Denied: Only ADMIN users can access Admin Dashboard.");
            this.currentPage = AppConfig.getDefaultPage();
            return;
        }

        if (page != null && !page.isEmpty()) {
            System.out.println("🔄 Switching to page: " + page);
            movieBean.init();
            this.currentPage = page;

        } else {
            System.out.println("❌ Error: Tried to set an empty page!");
            this.currentPage = AppConfig.getDefaultPage();
        }
    }

    /**
     * Navigates to the registration page.
     */
    public void goToRegister() {
        this.currentPage = "register";
        System.out.println("📌 Navigating to: Register Page");
    }

    /**
     * Navigates to the login page.
     */
    public void goToLogin() {
        this.currentPage = "login";
        System.out.println("📌 Navigating to: Login Page");
    }

    /**
     * Navigates to the home page.
     */
    public void goToHome() {
        this.currentPage = "home";
        System.out.println("🏠 Navigating to: Home Page");
    }

}
